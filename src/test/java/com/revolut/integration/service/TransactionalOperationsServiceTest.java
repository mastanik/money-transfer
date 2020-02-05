package com.revolut.integration.service;

import com.google.common.collect.Lists;
import com.revolut.integration.util.DbIntegrationTest;
import com.revolut.money_transfer.account.AccountService;
import com.revolut.money_transfer.bank.BankService;
import com.revolut.money_transfer.operation.OperationsService;
import com.revolut.money_transfer.operation.TransactionalOperationsService;
import com.revolut.money_transfer.operation.dto.DepositDto;
import com.revolut.money_transfer.operation.dto.TransferDto;
import com.revolut.money_transfer.operation.dto.WithdrawDto;
import org.jooq.Configuration;
import org.jooq.codegen.revolut.tables.daos.AccountDao;
import org.jooq.codegen.revolut.tables.daos.CurrencyExchangeRateDao;
import org.jooq.codegen.revolut.tables.pojos.Account;
import org.jooq.codegen.revolut.tables.pojos.CurrencyExchangeRate;
import org.jooq.codegen.revolut.tables.pojos.Customer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalOperationsServiceTest extends DbIntegrationTest {

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    @Test
    public void depositTest() {
        Customer customer = createRandomCustomer();
        Account account = createRandomAccount(customer, "USD", new BigDecimal("10"));
        OperationsService operationsService = injector.getInstance(OperationsService.class);

        operationsService.deposit(new DepositDto(account.getId(), new BigDecimal("5"), "USD"));

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccount = accountDao.findById(account.getId());
        assertEquals(new BigDecimal("15").setScale(4, RoundingMode.HALF_EVEN), storedAccount.getBalance());
    }

    @Test
    public void deposit_concurrentTest() {
        Customer customer = createRandomCustomer();
        Account account = createRandomAccount(customer, "USD", new BigDecimal("0"));
        OperationsService operationsService = injector.getInstance(OperationsService.class);

        int threadsNum = 30;
        submitTasksAndWait(threadsNum, new Thread(() -> operationsService.deposit(new DepositDto(account.getId(), new BigDecimal("1"), "USD"))));

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccount = accountDao.findById(account.getId());
        assertEquals(new BigDecimal(threadsNum).setScale(4, RoundingMode.HALF_EVEN), storedAccount.getBalance());
    }

    @Test
    public void deposit_Multicurrency_concurrentTest() {
        Customer customer = createRandomCustomer();
        Account account = createRandomAccount(customer, "USD", new BigDecimal("0"));
        OperationsService operationsService = injector.getInstance(OperationsService.class);

        int threadsNum = 30;
        CurrencyExchangeRate exchangeRateFrom = getCurrencyExchangeRate("GBP", "USD");
        BigDecimal expectedValue = new BigDecimal(threadsNum).multiply(exchangeRateFrom.getExchangeRate()).setScale(4, RoundingMode.HALF_EVEN);

        submitTasksAndWait(threadsNum, new Thread(() -> operationsService.deposit(new DepositDto(account.getId(), new BigDecimal("1"), "GBP"))));

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccount = accountDao.findById(account.getId());
        assertEquals(expectedValue, storedAccount.getBalance());
    }

    @Test
    public void withdrawTest() {
        Customer customer = createRandomCustomer();
        BigDecimal balance = new BigDecimal("100");
        Account account = createRandomAccount(customer, "USD", balance);
        OperationsService operationsService = injector.getInstance(OperationsService.class);

        BigDecimal toWithdraw = new BigDecimal("55");
        operationsService.withdraw(new WithdrawDto(account.getId(), toWithdraw, "USD"));

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccount = accountDao.findById(account.getId());
        assertEquals(balance.subtract(toWithdraw).setScale(4, RoundingMode.HALF_EVEN), storedAccount.getBalance());
    }

    @Test
    public void withdraw_concurrentTest() {
        Customer customer = createRandomCustomer();
        BigDecimal balance = new BigDecimal("100");
        Account account = createRandomAccount(customer, "USD", balance);

        BigDecimal toWithdraw = new BigDecimal("1");

        int threadsNum = 30;
        OperationsService operationsService = injector.getInstance(OperationsService.class);
        submitTasksAndWait(threadsNum, new Thread(() -> operationsService.withdraw(new WithdrawDto(account.getId(), toWithdraw, "USD"))));

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccount = accountDao.findById(account.getId());
        assertEquals(balance.subtract(toWithdraw.multiply(new BigDecimal(threadsNum))).setScale(4, RoundingMode.HALF_EVEN), storedAccount.getBalance());
    }

    @Test
    public void withdraw_Multicurrency_concurrentTest() {
        Customer customer = createRandomCustomer();
        BigDecimal balance = new BigDecimal("100");
        Account account = createRandomAccount(customer, "USD", balance);

        BigDecimal toWithdraw = new BigDecimal("1");

        int threadsNum = 30;
        OperationsService operationsService = injector.getInstance(OperationsService.class);
        submitTasksAndWait(threadsNum, new Thread(() -> operationsService.withdraw(new WithdrawDto(account.getId(), toWithdraw, "GBP"))));

        CurrencyExchangeRate exchangeRateFrom = getCurrencyExchangeRate("GBP", "USD");
        BigDecimal expectedValue = balance.subtract(
                new BigDecimal(threadsNum).multiply(exchangeRateFrom.getExchangeRate()).setScale(4, RoundingMode.HALF_EVEN))
                .setScale(4, RoundingMode.HALF_EVEN);

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccount = accountDao.findById(account.getId());
        assertEquals(expectedValue, storedAccount.getBalance());
    }

    @Test
    public void transfer_concurrentTest_crossTransfers() {
        Customer customerFrom = createRandomCustomer();
        Customer customerTo = createRandomCustomer();
        BigDecimal balanceFrom = new BigDecimal("100");
        BigDecimal balanceTo = new BigDecimal("100");
        Account accountFrom = createRandomAccount(customerFrom, "USD", balanceFrom);
        Account accountTo = createRandomAccount(customerTo, "USD", balanceTo);

        int numOfTransfers = 30;
        int numOfTransfersBack = 25;
        BigDecimal toTransfer = new BigDecimal("1");
        BigDecimal toTransferBack = new BigDecimal("1");

        OperationsService operationsService = injector.getInstance(OperationsService.class);
        submitTasksAndWait(numOfTransfers,
                new Thread(() -> operationsService.transfer(
                        new TransferDto(accountFrom.getId(), accountTo.getId(), toTransfer, "USD"))),
                numOfTransfersBack,
                new Thread(() -> operationsService.transfer(
                        new TransferDto(accountTo.getId(), accountFrom.getId(), toTransferBack, "USD")))
        );

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccountFrom = accountDao.findById(accountFrom.getId());
        Account storedAccountTo = accountDao.findById(accountTo.getId());

        BigDecimal expectedAccountFromBalance = balanceFrom.subtract(new BigDecimal(numOfTransfers)).add(new BigDecimal(numOfTransfersBack))
                .setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal expectedAccountToBalance = balanceTo.add(new BigDecimal(numOfTransfers)).subtract(new BigDecimal(numOfTransfersBack))
                .setScale(4, RoundingMode.HALF_EVEN);

        assertEquals(expectedAccountFromBalance, storedAccountFrom.getBalance());
        assertEquals(expectedAccountToBalance, storedAccountTo.getBalance());
    }

    @Test
    public void transfer_rollback() {
        Customer customerFrom = createRandomCustomer();
        Customer customerTo = createRandomCustomer();
        BigDecimal balanceFrom = new BigDecimal("100");
        BigDecimal balanceTo = new BigDecimal("100");
        Account accountFrom = createRandomAccount(customerFrom, "USD", balanceFrom);
        Account accountTo = createRandomAccount(customerTo, "USD", balanceTo);
        BigDecimal toTransfer = new BigDecimal("1");

        AccountService accountService = injector.getInstance(AccountService.class);
        AccountService spyAccountService = Mockito.spy(accountService);

        //Withdraw should work, deposit should fail
        Mockito
                .doCallRealMethod()
                .doThrow(new RuntimeException("Second Call Failed"))
                .when(spyAccountService).getById(any());

        BankService bankService = injector.getInstance(BankService.class);
        Configuration configuration = injector.getInstance(Configuration.class);

        OperationsService operationsService = new TransactionalOperationsService(spyAccountService, configuration, bankService);

        try {
            operationsService.transfer(new TransferDto(accountFrom.getId(), accountTo.getId(), toTransfer, "USD"));
        } catch (Exception e) {
        }

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccountFrom = accountDao.findById(accountFrom.getId());
        Account storedAccountTo = accountDao.findById(accountTo.getId());

        System.out.println(storedAccountFrom.getBalance());
        System.out.println(storedAccountTo.getBalance());
    }

    private void submitTasksAndWait(int numberOfTasks, Thread thread, int numberOfTasks2, Thread thread2) {
        List<Thread> threads = Lists.newArrayList();
        List<Future> submittedTasks = Lists.newArrayList();
        IntStream.rangeClosed(1, numberOfTasks).forEach(value -> threads.add(thread));
        IntStream.rangeClosed(1, numberOfTasks2).forEach(value -> threads.add(thread2));

        Collections.shuffle(threads);

        threads.forEach(threadToExecute -> submittedTasks.add(
                executorService.submit(threadToExecute)));

        submittedTasks.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    private void submitTasksAndWait(int numberOfTasks, Thread thread) {
        List<Future> submittedTasks = Lists.newArrayList();
        IntStream.rangeClosed(1, numberOfTasks).forEach(value -> submittedTasks.add(
                executorService.submit(thread)));

        submittedTasks.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    private CurrencyExchangeRate getCurrencyExchangeRate(String currencyFrom, String currencyTo) {
        CurrencyExchangeRateDao currencyExchangeRateDao = injector.getInstance(CurrencyExchangeRateDao.class);
        List<CurrencyExchangeRate> exchangeRates = currencyExchangeRateDao.fetchBySourceCurrency(currencyFrom);
        CurrencyExchangeRate exchangeRateFrom = exchangeRates.stream()
                .filter(currencyExchangeRate -> currencyExchangeRate.getTargetCurrency().equals(currencyTo))
                .findFirst().get();
        return exchangeRateFrom;
    }
}
