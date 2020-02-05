package com.revolut.integration;

import com.google.gson.reflect.TypeToken;
import com.revolut.integration.util.WebIntegrationTest;
import com.revolut.money_transfer.TestHttpClient.HttpClientResponse;
import com.revolut.money_transfer.customer.dto.CustomerDto;
import org.jooq.codegen.revolut.tables.daos.CustomerDao;
import org.jooq.codegen.revolut.tables.pojos.Customer;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CustomerIntegrationTest extends WebIntegrationTest {

    @Test
    public void createCustomer() {
        String createPayload = "{\"firstName\":\"Kobe\",\"lastName\":\"Bryant\",\"dateOfBirth\":\"1978-08-23\"}";
        HttpClientResponse res = testHttpClient.post(BASE_URL + "/customer", createPayload);
        assertEquals(200, res.getStatus());

        CustomerDto customerDto = gson.fromJson(res.getBody(), CustomerDto.class);
        assertEquals("Kobe", customerDto.getFirstName());
        assertEquals("Bryant", customerDto.getLastName());
        assertEquals(LocalDate.parse("1978-08-23"), customerDto.getDateOfBirth());
        assertNotNull(customerDto.getId());
    }

    @Test
    public void findCustomers() {
        createRandomCustomer();
        createRandomCustomer();

        HttpClientResponse res = testHttpClient.get(BASE_URL + "/customers");
        assertEquals(200, res.getStatus());

        List<CustomerDto> fetchedCustomers = gson.fromJson(res.getBody(),
                new TypeToken<List<CustomerDto>>() {
                }.getType());

        CustomerDao customerDao = injector.getInstance(CustomerDao.class);
        List<Customer> storedCustomers = customerDao.findAll();
        assertEquals(storedCustomers.size(), fetchedCustomers.size());
        fetchedCustomers.forEach(customerDto -> assertNotNull(customerDto.getId()));

        List<Long> storedIds = storedCustomers.stream().map(Customer::getId).collect(Collectors.toList());
        List<Long> fetchedIds = fetchedCustomers.stream().map(CustomerDto::getId).collect(Collectors.toList());

        assertTrue(fetchedIds.containsAll(storedIds));
    }

    @Test
    public void findCustomerById() {
        createRandomCustomer();

        CustomerDao customerDao = injector.getInstance(CustomerDao.class);
        List<Customer> customers = customerDao.findAll();
        Customer storedCustomer = customers.iterator().next();

        HttpClientResponse res = testHttpClient.get(BASE_URL + "/customer/" + storedCustomer.getId());
        CustomerDto fetchedCustomer = gson.fromJson(res.getBody(), CustomerDto.class);

        assertEquals(storedCustomer.getId(), fetchedCustomer.getId());
        assertEquals(storedCustomer.getLastName(), fetchedCustomer.getLastName());
        assertEquals(storedCustomer.getFirstName(), fetchedCustomer.getFirstName());
        assertEquals(LocalDate.parse(storedCustomer.getDateOfBirth()), fetchedCustomer.getDateOfBirth());
    }

}
