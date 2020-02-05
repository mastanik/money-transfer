package com.revolut.integration;

import com.revolut.integration.util.WebIntegrationTest;
import com.revolut.money_transfer.TestHttpClient.HttpClientResponse;
import org.jooq.codegen.revolut.tables.daos.AccountDao;
import org.jooq.codegen.revolut.tables.daos.CurrencyExchangeRateDao;
import org.jooq.codegen.revolut.tables.pojos.Account;
import org.jooq.codegen.revolut.tables.pojos.CurrencyExchangeRate;
import org.jooq.codegen.revolut.tables.pojos.Customer;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OperationsIntegrationTest extends WebIntegrationTest {

    @Test
    public void deposit() {
        Customer customer = createRandomCustomer();
        Account account = createRandomAccount(customer, "USD", new BigDecimal("0"));

        BigDecimal depositedAmount = new BigDecimal("50");
        String createPayload = String.format("{\"accountId\":%s,\"amount\":%s,\"currency\":\"USD\"}", account.getId(), depositedAmount);
        HttpClientResponse res = testHttpClient.post(BASE_URL + "/operation/deposit", createPayload);
        assertEquals(200, res.getStatus());

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account updatedAccount = accountDao.findById(account.getId());
        assertEquals(depositedAmount.setScale(4, RoundingMode.HALF_EVEN), updatedAccount.getBalance());
    }

    @Test
    public void deposit_NoExchangeRate() {
        Customer customer = createRandomCustomer();
        BigDecimal initialBalance = new BigDecimal("0");
        Account account = createRandomAccount(customer, "USD", initialBalance);

        BigDecimal depositedAmount = new BigDecimal("50");
        String createPayload = String.format("{\"accountId\":%s,\"amount\":%s,\"currency\":\"NOT_EXISTING_CURRENCY\"}", account.getId(), depositedAmount);
        HttpClientResponse res = testHttpClient.post(BASE_URL + "/operation/deposit", createPayload);
        assertEquals(409, res.getStatus());

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account updatedAccount = accountDao.findById(account.getId());
        assertEquals(initialBalance.setScale(4, RoundingMode.HALF_EVEN), updatedAccount.getBalance());
    }

    @Test
    public void withdraw() {
        Customer customer = createRandomCustomer();
        BigDecimal accountBalance = new BigDecimal("100");
        Account account = createRandomAccount(customer, "USD", accountBalance);
        BigDecimal withdrawAmount = new BigDecimal("50.50");
        String withdrawPayload = String.format("{\"accountId\":%s,\"amount\":%s,\"currency\":\"USD\"}", account.getId(), withdrawAmount);

        HttpClientResponse res = testHttpClient.post(BASE_URL + "/operation/withdraw", withdrawPayload);
        assertEquals(200, res.getStatus());

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account updatedAccount = accountDao.findById(account.getId());
        assertEquals(accountBalance.subtract(withdrawAmount).setScale(4, RoundingMode.HALF_EVEN), updatedAccount.getBalance());
    }

    @Test
    public void withdraw_NotEnoughFunds() {
        Customer customer = createRandomCustomer();
        BigDecimal accountBalance = new BigDecimal("100");
        Account account = createRandomAccount(customer, "USD", accountBalance);
        BigDecimal withdrawAmount = new BigDecimal("150.50");
        String withdrawPayload = String.format("{\"accountId\":%s,\"amount\":%s,\"currency\":\"USD\"}", account.getId(), withdrawAmount);

        HttpClientResponse res = testHttpClient.post(BASE_URL + "/operation/withdraw", withdrawPayload);
        assertEquals(409, res.getStatus());

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccount = accountDao.findById(account.getId());
        assertEquals(accountBalance.setScale(4, RoundingMode.HALF_EVEN), storedAccount.getBalance());
    }

    @Test
    public void withdraw_NoExchangeRate() {
        Customer customer = createRandomCustomer();
        BigDecimal accountBalance = new BigDecimal("100");
        Account account = createRandomAccount(customer, "USD", accountBalance);
        BigDecimal withdrawAmount = new BigDecimal("75");
        String withdrawPayload = String.format("{\"accountId\":%s,\"amount\":%s,\"currency\":\"NOT_EXISTING_CURRENCY\"}", account.getId(), withdrawAmount);

        HttpClientResponse res = testHttpClient.post(BASE_URL + "/operation/withdraw", withdrawPayload);
        assertEquals(409, res.getStatus());

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccount = accountDao.findById(account.getId());
        assertEquals(accountBalance.setScale(4, RoundingMode.HALF_EVEN), storedAccount.getBalance());
    }

    @Test
    public void transfer() {
        Customer customerFrom = createRandomCustomer();
        Customer customerTo = createRandomCustomer();
        BigDecimal accountBalanceFrom = new BigDecimal("100");
        BigDecimal accountBalanceTo = new BigDecimal("15");
        BigDecimal amountToTransfer = new BigDecimal("75");
        Account accountFrom = createRandomAccount(customerFrom, "USD", accountBalanceFrom);
        Account accountTo = createRandomAccount(customerTo, "USD", accountBalanceTo);

        String transferPayload = String.format("{\"accountIdFrom\":%s,\"accountIdTo\":%s,\"amount\":%s,\"currency\":\"USD\"}",
                accountFrom.getId(), accountTo.getId(), amountToTransfer);
        HttpClientResponse res = testHttpClient.post(BASE_URL + "/operation/transfer", transferPayload);
        assertEquals(200, res.getStatus());

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccountFrom = accountDao.findById(accountFrom.getId());
        Account storedAccountTo = accountDao.findById(accountTo.getId());

        assertEquals(accountBalanceFrom.subtract(amountToTransfer).setScale(4, RoundingMode.HALF_EVEN), storedAccountFrom.getBalance());
        assertEquals(accountBalanceTo.add(amountToTransfer).setScale(4, RoundingMode.HALF_EVEN), storedAccountTo.getBalance());
    }

    @Test
    public void transfer_multiCurrency() {
        Customer customerFrom = createRandomCustomer();
        Customer customerTo = createRandomCustomer();
        BigDecimal accountBalanceFrom = new BigDecimal("100");
        BigDecimal accountBalanceTo = new BigDecimal("15");
        BigDecimal amountToTransfer = new BigDecimal("75");
        Account accountFrom = createRandomAccount(customerFrom, "USD", accountBalanceFrom);
        Account accountTo = createRandomAccount(customerTo, "EUR", accountBalanceTo);

        String transferPayload = String.format("{\"accountIdFrom\":%s,\"accountIdTo\":%s,\"amount\":%s,\"currency\":\"GBP\"}",
                accountFrom.getId(), accountTo.getId(), amountToTransfer);
        HttpClientResponse res = testHttpClient.post(BASE_URL + "/operation/transfer", transferPayload);
        assertEquals(200, res.getStatus());

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccountFrom = accountDao.findById(accountFrom.getId());
        Account storedAccountTo = accountDao.findById(accountTo.getId());

        CurrencyExchangeRateDao currencyExchangeRateDao = injector.getInstance(CurrencyExchangeRateDao.class);
        List<CurrencyExchangeRate> exchangeRates = currencyExchangeRateDao.fetchBySourceCurrency("GBP");

        CurrencyExchangeRate exchangeRateFrom = exchangeRates.stream()
                .filter(currencyExchangeRate -> currencyExchangeRate.getTargetCurrency().equals("USD"))
                .findFirst().get();
        CurrencyExchangeRate exchangeRateTo = exchangeRates.stream()
                .filter(currencyExchangeRate -> currencyExchangeRate.getTargetCurrency().equals("EUR"))
                .findFirst().get();

        BigDecimal exchangedFrom = amountToTransfer.multiply(exchangeRateFrom.getExchangeRate()).setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal exchangedTo = amountToTransfer.multiply(exchangeRateTo.getExchangeRate()).setScale(4, RoundingMode.HALF_EVEN);

        assertEquals(accountBalanceFrom.subtract(exchangedFrom).setScale(4, RoundingMode.HALF_EVEN), storedAccountFrom.getBalance());
        assertEquals(accountBalanceTo.add(exchangedTo).setScale(4, RoundingMode.HALF_EVEN), storedAccountTo.getBalance());
    }

    @Test
    public void transfer_NotEnoughFunds() {
        Customer customerFrom = createRandomCustomer();
        Customer customerTo = createRandomCustomer();
        BigDecimal accountBalanceFrom = new BigDecimal("100");
        BigDecimal accountBalanceTo = new BigDecimal("15");
        BigDecimal amountToTransfer = new BigDecimal("750");
        Account accountFrom = createRandomAccount(customerFrom, "USD", accountBalanceFrom);
        Account accountTo = createRandomAccount(customerTo, "USD", accountBalanceTo);

        String transferPayload = String.format("{\"accountIdFrom\":%s,\"accountIdTo\":%s,\"amount\":%s,\"currency\":\"USD\"}",
                accountFrom.getId(), accountTo.getId(), amountToTransfer);
        HttpClientResponse res = testHttpClient.post(BASE_URL + "/operation/transfer", transferPayload);
        assertEquals(409, res.getStatus());

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccountFrom = accountDao.findById(accountFrom.getId());
        Account storedAccountTo = accountDao.findById(accountTo.getId());

        assertEquals(accountBalanceFrom.setScale(4, RoundingMode.HALF_EVEN), storedAccountFrom.getBalance());
        assertEquals(accountBalanceTo.setScale(4, RoundingMode.HALF_EVEN), storedAccountTo.getBalance());
    }

    @Test
    public void transfer_NoExchangeRate() {
        Customer customerFrom = createRandomCustomer();
        Customer customerTo = createRandomCustomer();
        BigDecimal accountBalanceFrom = new BigDecimal("100");
        BigDecimal accountBalanceTo = new BigDecimal("15");
        BigDecimal amountToTransfer = new BigDecimal("75");
        Account accountFrom = createRandomAccount(customerFrom, "USD", accountBalanceFrom);
        Account accountTo = createRandomAccount(customerTo, "USD", accountBalanceTo);

        String transferPayload = String.format("{\"accountIdFrom\":%s,\"accountIdTo\":%s,\"amount\":%s,\"currency\":\"NOT_EXISTING_CURRENCY\"}",
                accountFrom.getId(), accountTo.getId(), amountToTransfer);
        HttpClientResponse res = testHttpClient.post(BASE_URL + "/operation/transfer", transferPayload);
        assertEquals(409, res.getStatus());

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccountFrom = accountDao.findById(accountFrom.getId());
        Account storedAccountTo = accountDao.findById(accountTo.getId());

        assertEquals(accountBalanceFrom.setScale(4, RoundingMode.HALF_EVEN), storedAccountFrom.getBalance());
        assertEquals(accountBalanceTo.setScale(4, RoundingMode.HALF_EVEN), storedAccountTo.getBalance());
    }
}