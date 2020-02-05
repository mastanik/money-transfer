package com.revolut.integration;

import com.google.gson.reflect.TypeToken;
import com.revolut.integration.util.WebIntegrationTest;
import com.revolut.money_transfer.TestHttpClient.HttpClientResponse;
import com.revolut.money_transfer.account.dto.AccountDto;
import org.jooq.codegen.revolut.tables.daos.AccountDao;
import org.jooq.codegen.revolut.tables.pojos.Account;
import org.jooq.codegen.revolut.tables.pojos.Customer;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AccountIntegrationTest extends WebIntegrationTest {

    @Test
    public void createAccount() {
        Customer customer = createRandomCustomer();

        String createPayload = String.format("{\"customerId\":%s,\"currency\":\"USD\"}", customer.getId());
        HttpClientResponse res = testHttpClient.post(BASE_URL + "/account", createPayload);
        assertEquals(200, res.getStatus());

        AccountDto accountDto = gson.fromJson(res.getBody(), AccountDto.class);

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account storedAccount = accountDao.findById(accountDto.getId());
        assertEquals(storedAccount.getCurrency(), accountDto.getCurrency());
        assertEquals(storedAccount.getBalance(), accountDto.getBalance());
        assertEquals(storedAccount.getCustomerId(), accountDto.getCustomerId());
    }

    @Test
    public void createAccount_NotExistingCurrency() {
        Customer customer = createRandomCustomer();

        String createPayload = String.format("{\"customerId\":%s,\"currency\":\"NOT_EXISTING_CURRENCY\"}", customer.getId());
        HttpClientResponse res = testHttpClient.post(BASE_URL + "/account", createPayload);
        assertEquals(400, res.getStatus());
    }

    @Test
    public void createAccount_NotExistingUser() {
        String createPayload = String.format("{\"customerId\":%s,\"currency\":\"USD\"}", -9_999_999);
        HttpClientResponse res = testHttpClient.post(BASE_URL + "/account", createPayload);
        assertEquals(404, res.getStatus());
    }

    @Test
    public void findAll() {
        Customer customer = createRandomCustomer();
        createRandomAccount(customer, "USD", new BigDecimal("0"));
        createRandomAccount(customer, "EUR", new BigDecimal("0"));

        HttpClientResponse res = testHttpClient.get(BASE_URL + "/accounts");
        assertEquals(200, res.getStatus());

        List<AccountDto> fetchedAccounts = gson.fromJson(res.getBody(),
                new TypeToken<List<AccountDto>>() {
                }.getType());

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        List<Account> storedAccounts = accountDao.findAll();
        assertEquals(storedAccounts.size(), fetchedAccounts.size());
        fetchedAccounts.forEach(accountDto -> assertNotNull(accountDto.getId()));

        List<Long> storedIds = storedAccounts.stream().map(Account::getId).collect(Collectors.toList());
        List<Long> fetchedIds = fetchedAccounts.stream().map(AccountDto::getId).collect(Collectors.toList());

        assertTrue(fetchedIds.containsAll(storedIds));
    }

    @Test
    public void findAccountById() {
        Customer customer = createRandomCustomer();
        createRandomAccount(customer, "USD", new BigDecimal("0"));
        createRandomAccount(customer, "EUR", new BigDecimal("0"));

        AccountDao accountDao = injector.getInstance(AccountDao.class);
        List<Account> accounts = accountDao.findAll();
        Account storedAccount = accounts.iterator().next();

        HttpClientResponse res = testHttpClient.get(BASE_URL + "/account/" + storedAccount.getId());
        assertEquals(200, res.getStatus());
        AccountDto fetchedAccount = gson.fromJson(res.getBody(), AccountDto.class);

        assertEquals(storedAccount.getId(), fetchedAccount.getId());
        assertEquals(storedAccount.getBalance(), fetchedAccount.getBalance());
        assertEquals(storedAccount.getCurrency(), fetchedAccount.getCurrency());
        assertEquals(storedAccount.getCustomerId(), fetchedAccount.getCustomerId());
    }

    @Test
    public void findAccountById_NotExisting() {
        HttpClientResponse res = testHttpClient.get(BASE_URL + "/account/" + -9_999_999);
        assertEquals(404, res.getStatus());
    }
}
