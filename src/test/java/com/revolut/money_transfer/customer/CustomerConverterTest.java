package com.revolut.money_transfer.customer;

import com.revolut.money_transfer.customer.dto.CustomerDto;
import org.jooq.codegen.revolut.tables.pojos.Customer;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class CustomerConverterTest {

    private CustomerConverter converter = new CustomerConverter();

    @Test
    public void test_Convert() {

        CustomerDto expected = new CustomerDto.CustomerDtoBuilder()
                .id(8L)
                .dateOfBirth(LocalDate.parse("1978-08-23"))
                .firstName("Kobe")
                .lastName("Bryant")
                .build();

        Customer customer = new Customer();
        customer.setId(8L);
        customer.setDateOfBirth("1978-08-23");
        customer.setFirstName("Kobe");
        customer.setLastName("Bryant");

        CustomerDto dto = converter.convert(customer);

        assertEquals(expected.getId(), dto.getId());
        assertEquals(expected.getDateOfBirth(), dto.getDateOfBirth());
        assertEquals(expected.getFirstName(), dto.getFirstName());
        assertEquals(expected.getLastName(), dto.getLastName());
    }
}
