package com.revolut.money_transfer.account;

import com.google.inject.Inject;
import com.revolut.money_transfer.account.dto.AccountCreateDto;
import com.revolut.money_transfer.account.dto.AccountDto;
import com.revolut.money_transfer.account.exception.CurrencyNotSupportedException;
import com.revolut.money_transfer.api.exception.ErrorCodes;
import com.revolut.money_transfer.api.exception.ResourceNotFoundException;
import com.revolut.money_transfer.bank.BankService;
import com.revolut.money_transfer.customer.CustomerService;
import com.revolut.money_transfer.customer.dto.CustomerDto;
import org.jooq.codegen.revolut.tables.daos.AccountDao;
import org.jooq.codegen.revolut.tables.pojos.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccountService {

    private final AccountConverter accountConverter;
    private final AccountDao accountDao;
    private final CustomerService customerService;
    private final BankService bankService;

    @Inject
    public AccountService(AccountConverter accountConverter, AccountDao accountDao, CustomerService customerService, BankService bankService) {
        this.accountConverter = accountConverter;
        this.accountDao = accountDao;
        this.customerService = customerService;
        this.bankService = bankService;
    }

    public List<AccountDto> findAll() {
        List<Account> accounts = accountDao.findAll();
        return accounts.stream()
                .map(accountConverter::convert)
                .collect(Collectors.toList());
    }

    public AccountDto getById(Long id) {
        return accountConverter.convert(getAccountById(id));
    }

    private Account getAccountById(Long id) {
        return Optional.ofNullable(accountDao.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Account with id %d not found", id), ErrorCodes.ACCOUNT_NOT_FOUND));
    }

    public AccountDto create(AccountCreateDto createDto) {
        if (!bankService.isCurrencySupported(createDto.getCurrency())) {
            throw new CurrencyNotSupportedException(String.format("Currency %s not supported", createDto.getCurrency()), ErrorCodes.CURRENCY_NOT_SUPPORTED);
        }

        CustomerDto customerDto = customerService.getById(createDto.getCustomerId());

        Account account = new Account();
        account.setBalance(new BigDecimal(0));
        account.setCurrency(createDto.getCurrency());
        account.setCustomerId(customerDto.getId());
        account.setVersion(1L);
        accountDao.insert(account);

        return accountConverter.convert(accountDao.findById(accountDao.getId(account)));
    }

}
