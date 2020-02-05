package com.revolut.money_transfer.account;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.revolut.App;
import com.revolut.money_transfer.TestHttpClient;
import com.revolut.money_transfer.TestHttpClient.HttpClientResponse;
import com.revolut.money_transfer.account.dto.AccountDto;
import com.revolut.money_transfer.api.configuration.GsonConfiguration;
import com.revolut.money_transfer.api.exception.ErrorCodes;
import com.revolut.money_transfer.api.exception.ResourceNotFoundException;
import com.revolut.money_transfer.customer.CustomerEndpoint;
import com.revolut.money_transfer.operation.OperationsEndpoint;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

@RunWith(MockitoJUnitRunner.class)
public class AccountEndpointTest {

    private static boolean setUpIsDone = false;
    @Mock
    private CustomerEndpoint customerEndpoint;
    @Mock
    private OperationsEndpoint operationsEndpoint;
    @Mock
    private AccountService accountService;
    private Gson gson = new GsonConfiguration().get();
    private TestHttpClient testHttpClient = new TestHttpClient();

    private App app;

    private AccountDto account = new AccountDto.AccountDtoBuilder().id(1L).customerId(1L).currency("USD").balance(new BigDecimal("0")).build();

    private List<AccountDto> accounts = Lists.newArrayList(
            account,
            new AccountDto.AccountDtoBuilder().id(2L).customerId(2L).currency("EUR").balance(new BigDecimal("1")).build(),
            new AccountDto.AccountDtoBuilder().id(3L).customerId(3L).currency("GBP").balance(new BigDecimal("2")).build()
    );

    @Before
    public void setup() {
        if (!setUpIsDone) {
            when(accountService.findAll()).thenReturn(accounts);
            when(accountService.getById(1L)).thenReturn(account);
            when(accountService.getById(9999L)).thenThrow(new ResourceNotFoundException("Not Found", ErrorCodes.ACCOUNT_NOT_FOUND));
            when(accountService.create(any())).thenReturn(account);
            app = new App(new AccountEndpoint(accountService, gson), customerEndpoint, operationsEndpoint, gson);
            app.init();
            awaitInitialization();
        }
        setUpIsDone = true;
    }

    @Test
    public void test_findAll() {
        HttpClientResponse res = testHttpClient.get("http://localhost:4567/accounts");
        assertEquals(gson.toJson(accounts), res.getBody());
        assertEquals(200, res.getStatus());
    }

    @Test
    public void test_getById() {
        HttpClientResponse res = testHttpClient.get("http://localhost:4567/account/1");
        assertEquals(gson.toJson(account), res.getBody());
        assertEquals(200, res.getStatus());
    }

    @Test
    public void test_getById_BadId() {
        HttpClientResponse res = testHttpClient.get("http://localhost:4567/account/test");
        assertEquals(400, res.getStatus());
    }

    @Test
    public void test_getById_NotFound() {
        HttpClientResponse res = testHttpClient.get("http://localhost:4567/account/9999");
        assertEquals(404, res.getStatus());
    }

    @Test
    public void test_create() {
        String createPayload = "{\"customerId\":1,\"currency\":\"USD\"}";
        HttpClientResponse res = testHttpClient.post("http://localhost:4567/account", createPayload);
        assertEquals(200, res.getStatus());
        assertEquals(gson.toJson(account), res.getBody());
    }

    @Test
    public void test_create_ValidationFails_idString() {
        String createPayload = "{\"customerId\":\"test\",\"currency\":\"USD\"}";
        HttpClientResponse res = testHttpClient.post("http://localhost:4567/account", createPayload);
        assertEquals(400, res.getStatus());
    }

    @Test
    public void test_create_ValidationFails_BadJson() {
        String createPayload = "{customerId\"test\";\"currency\":\"USD\"}";
        HttpClientResponse res = testHttpClient.post("http://localhost:4567/account", createPayload);
        assertEquals(400, res.getStatus());
    }

    @AfterClass
    public static void tearDown() {
        stop();
    }

}
