package com.revolut.money_transfer.customer;

import com.google.common.collect.Lists;
import com.revolut.money_transfer.api.exception.ResourceNotFoundException;
import com.revolut.money_transfer.customer.dto.CustomerCreateDto;
import com.revolut.money_transfer.customer.dto.CustomerDto;
import org.jooq.codegen.revolut.tables.daos.CustomerDao;
import org.jooq.codegen.revolut.tables.pojos.Customer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomerServiceTest {

    @Mock
    private CustomerDao customerDao;
    private CustomerService customerService;

    private Customer customer = new Customer(8L, "Kobe", "Bryant", "1978-08-23");

    private List<Customer> customers = Lists.newArrayList(
            customer,
            new Customer(9L, "Test", "Test", "1999-09-09"),
            new Customer(10L, "Test", "Test", "1999-09-09")
    );

    @Before
    public void setup() {
        when(customerDao.findAll()).thenReturn(customers);
        when(customerDao.findById(8L)).thenReturn(customer);
        when(customerDao.findById(9999L)).thenReturn(null);
        customerService = new CustomerService(new CustomerConverter(), customerDao);
    }

    @Test
    public void test_FindAll() {
        List<CustomerDto> customers = customerService.findAll();
        assertEquals(3, customers.size());
    }

    @Test
    public void test_getById() {
        CustomerDto dto = customerService.getById(8L);
        assertEquals(customer.getId(), dto.getId());
        assertEquals(LocalDate.parse(customer.getDateOfBirth()), dto.getDateOfBirth());
        assertEquals(customer.getFirstName(), dto.getFirstName());
        assertEquals(customer.getLastName(), dto.getLastName());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void test_getById_Null() {
        customerService.getById(9999L);
    }

    @Test
    public void test_Create() {
        CustomerCreateDto createDto = new CustomerCreateDto("Kobe", "Bryant", "1978-08-23");
        CustomerDto dto = customerService.create(createDto);
        assertEquals("Kobe", dto.getFirstName());
        assertEquals("Bryant", dto.getLastName());
        assertEquals(LocalDate.parse("1978-08-23"), dto.getDateOfBirth());
    }

}
