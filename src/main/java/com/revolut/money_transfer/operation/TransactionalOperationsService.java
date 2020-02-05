package com.revolut.money_transfer.operation;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Striped;
import com.google.inject.Inject;
import com.revolut.money_transfer.account.AccountService;
import com.revolut.money_transfer.account.dto.AccountDto;
import com.revolut.money_transfer.api.exception.ConflictException;
import com.revolut.money_transfer.api.exception.ErrorCodes;
import com.revolut.money_transfer.bank.BankService;
import com.revolut.money_transfer.bank.Money;
import com.revolut.money_transfer.operation.dto.DepositDto;
import com.revolut.money_transfer.operation.dto.TransferDto;
import com.revolut.money_transfer.operation.dto.WithdrawDto;
import org.jooq.Configuration;
import org.jooq.codegen.revolut.tables.records.AccountRecord;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;

import static org.jooq.codegen.revolut.tables.Account.ACCOUNT;

public class TransactionalOperationsService implements OperationsService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionalOperationsService.class);

    private final AccountService accountService;
    private final Configuration configuration;
    private final BankService bankService;
    private final Striped<Lock> stripedLocks;

    @Inject
    public TransactionalOperationsService(AccountService accountService, Configuration configuration, BankService bankService) {
        this.accountService = accountService;
        this.configuration = configuration;
        this.bankService = bankService;
        this.stripedLocks = Striped.lock(16);
    }

    public void deposit(final DepositDto depositDto) {
        deposit(depositDto, this.configuration);
    }

    public void withdraw(final WithdrawDto withdrawDto) {
        withdraw(withdrawDto, this.configuration);
    }

    private void deposit(final DepositDto depositDto, Configuration configuration) {
        Long accountId = depositDto.getAccountId();
        Lock lock = stripedLocks.get(accountId);
        lock.lock();
        try {
            AccountDto accountDto = accountService.getById(accountId);
            Money toDeposit = new Money(depositDto.getAmount(), depositDto.getCurrency());
            Money exchangedToDeposit = bankService.exchange(toDeposit, accountDto.getCurrency());
            Money sum = exchangedToDeposit.add(new Money(accountDto.getBalance(), accountDto.getCurrency()));

            logger.debug("Deposit {} {} to account {} with current balance {} {}", toDeposit.getAmount(), toDeposit.getCurrency(), accountDto.getId(), accountDto.getBalance(), accountDto.getCurrency());

            configuration.dsl().transaction(nestedConfiguration -> this.updateAccountBalance(accountDto.getId(),
                    sum.getAmount(),
                    nestedConfiguration));
        } finally {
            lock.unlock();
        }
    }

    private void withdraw(final WithdrawDto withdrawDto, Configuration configuration) {
        Long accountId = withdrawDto.getAccountId();
        Lock lock = stripedLocks.get(accountId);
        lock.lock();
        try {
            AccountDto accountDto = accountService.getById(withdrawDto.getAccountId());
            Money toWithdraw = new Money(withdrawDto.getAmount(), withdrawDto.getCurrency());
            Money exchangedToWithdraw = bankService.exchange(toWithdraw, accountDto.getCurrency());
            checkWithdrawalPossibility(accountDto, exchangedToWithdraw.getAmount());
            Money sub = new Money(accountDto.getBalance(), accountDto.getCurrency()).subtract(exchangedToWithdraw);

            logger.debug("Withdraw {} {} from account {} with current balance {} {}", toWithdraw.getAmount(), toWithdraw.getCurrency(), accountDto.getId(), accountDto.getBalance(), accountDto.getCurrency());

            configuration.dsl().transaction(nestedConfiguration -> this.updateAccountBalance(accountDto.getId(),
                    sub.getAmount(),
                    nestedConfiguration));
        } finally {
            lock.unlock();
        }
    }

    public void transfer(final TransferDto transferDto) {
        Long accountIdFrom = transferDto.getAccountIdFrom();
        Long accountIdTo = transferDto.getAccountIdTo();
        Iterable<Lock> locks = stripedLocks.bulkGet(Lists.newArrayList(accountIdFrom, accountIdTo));
        locks.forEach(Lock::lock);
        try {
            configuration.dsl().transaction(nestedConfiguration -> {
                BigDecimal transferAmount = transferDto.getAmount();

                logger.debug("Transfer {} {} from account {} to account {}",
                        transferAmount, transferDto.getCurrency(), accountIdFrom, accountIdTo);

                this.withdraw(new WithdrawDto(accountIdFrom, transferAmount, transferDto.getCurrency()), nestedConfiguration);
                this.deposit(new DepositDto(accountIdTo, transferAmount, transferDto.getCurrency()), nestedConfiguration);
            });
        } finally {
            locks.forEach(Lock::unlock);
        }
    }

    private void updateAccountBalance(Long accountId, BigDecimal amount, Configuration configuration) {
        AccountRecord accountRecord = DSL.using(configuration).fetchOne(ACCOUNT, ACCOUNT.ID.eq(accountId));
        accountRecord.setBalance(amount);
        accountRecord.store();
    }

    private void checkWithdrawalPossibility(AccountDto accountDto, BigDecimal amount) {
        if (accountDto.getBalance().compareTo(amount) < 0) {
            throw new ConflictException("Insufficient funds", ErrorCodes.INSUFFICIENT_FUNDS);
        }
    }
}
