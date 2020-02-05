package com.revolut.money_transfer.customer;

import com.revolut.money_transfer.customer.dto.CustomerDto;
import org.jooq.codegen.revolut.tables.pojos.Customer;

import java.time.LocalDate;

public class CustomerConverter {

    public CustomerDto convert(Customer customer) {
        return new CustomerDto.CustomerDtoBuilder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .dateOfBirth(LocalDate.parse(customer.getDateOfBirth()))
                .build();
    }
}
