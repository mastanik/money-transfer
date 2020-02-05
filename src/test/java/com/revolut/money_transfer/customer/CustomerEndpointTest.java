package com.revolut.money_transfer.customer;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.revolut.App;
import com.revolut.money_transfer.TestHttpClient;
import com.revolut.money_transfer.TestHttpClient.HttpClientResponse;
import com.revolut.money_transfer.account.AccountEndpoint;
import com.revolut.money_transfer.api.configuration.GsonConfiguration;
import com.revolut.money_transfer.api.exception.ErrorCodes;
import com.revolut.money_transfer.api.exception.ResourceNotFoundException;
import com.revolut.money_transfer.customer.dto.CustomerDto;
import com.revolut.money_transfer.operation.OperationsEndpoint;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

@RunWith(MockitoJUnitRunner.class)
public class CustomerEndpointTest {

    private static boolean setUpIsDone = false;
    @Mock
    private AccountEndpoint accountEndpoint;
    @Mock
    private OperationsEndpoint operationsEndpoint;
    @Mock
    private CustomerService customerService;
    private Gson gson = new GsonConfiguration().get();
    private TestHttpClient testHttpClient = new TestHttpClient();

    private App app;

    private CustomerDto customer = new CustomerDto.CustomerDtoBuilder().id(8L).firstName("Kobe").lastName("Bryant").dateOfBirth(LocalDate.parse("1978-08-23")).build();

    private List<CustomerDto> customers = Lists.newArrayList(
            customer,
            new CustomerDto.CustomerDtoBuilder().id(9L).firstName("Test").lastName("Test").dateOfBirth(LocalDate.parse("1999-09-09")).build(),
            new CustomerDto.CustomerDtoBuilder().id(10L).firstName("Test").lastName("Test").dateOfBirth(LocalDate.parse("1999-09-09")).build()
    );

    @Before
    public void setup() {
        if (!setUpIsDone) {
            when(customerService.findAll()).thenReturn(customers);
            when(customerService.getById(8L)).thenReturn(customer);
            when(customerService.getById(9999L)).thenThrow(new ResourceNotFoundException("Not Found", ErrorCodes.CUSTOMER_NOT_FOUND));
            when(customerService.create(any())).thenReturn(customer);
            app = new App(accountEndpoint, new CustomerEndpoint(customerService, gson), operationsEndpoint, gson);
            app.init();
            awaitInitialization();
        }
        setUpIsDone = true;
    }

    @Test
    public void test_findAll() {
        HttpClientResponse res = testHttpClient.get("http://localhost:4567/customers");
        assertEquals(gson.toJson(customers), res.getBody());
        assertEquals(200, res.getStatus());
    }

    @Test
    public void test_getById() {
        HttpClientResponse res = testHttpClient.get("http://localhost:4567/customer/8");
        assertEquals(gson.toJson(customer), res.getBody());
        assertEquals(200, res.getStatus());
    }

    @Test
    public void test_getById_BadId() {
        HttpClientResponse res = testHttpClient.get("http://localhost:4567/customer/test");
        assertEquals(400, res.getStatus());
    }

    @Test
    public void test_getById_NotFound() {
        HttpClientResponse res = testHttpClient.get("http://localhost:4567/customer/9999");
        assertEquals(404, res.getStatus());
    }

    @Test
    public void test_create() {
        String createPayload = "{\"firstName\":\"Kobe\",\"lastName\":\"Bryant\",\"dateOfBirth\":\"1978-08-23\"}";
        HttpClientResponse res = testHttpClient.post("http://localhost:4567/customer", createPayload);
        assertEquals(200, res.getStatus());
        assertEquals(gson.toJson(customer), res.getBody());
    }

    @Test
    public void test_create_ValidationFails() {
        String createPayload = "{\"lastNameTestWrong\":\"Bryant\",\"dateOfBirth\":\"test-1978-08-23\"}";
        HttpClientResponse res = testHttpClient.post("http://localhost:4567/customer", createPayload);
        assertEquals(400, res.getStatus());
    }

    @Test
    public void test_create_ValidationFails_BadJson() {
        String createPayload = "{firstName,Kobe\",\"lastName\":\"Bryant\",\"dateOfBirth\":\"1978-08-23\"}";
        HttpClientResponse res = testHttpClient.post("http://localhost:4567/customer", createPayload);
        assertEquals(400, res.getStatus());
    }

    @AfterClass
    public static void tearDown() {
        stop();
    }

}
