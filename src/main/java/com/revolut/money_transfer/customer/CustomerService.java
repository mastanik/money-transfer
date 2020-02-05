package com.revolut.money_transfer.customer;

import com.google.inject.Inject;
import com.revolut.money_transfer.api.exception.ErrorCodes;
import com.revolut.money_transfer.api.exception.ResourceNotFoundException;
import com.revolut.money_transfer.customer.dto.CustomerCreateDto;
import com.revolut.money_transfer.customer.dto.CustomerDto;
import org.jooq.codegen.revolut.tables.daos.CustomerDao;
import org.jooq.codegen.revolut.tables.pojos.Customer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomerService {

    private final CustomerConverter customerConverter;
    private final CustomerDao customerDao;

    @Inject
    public CustomerService(CustomerConverter customerConverter, CustomerDao customerDao) {
        this.customerConverter = customerConverter;
        this.customerDao = customerDao;
    }

    public List<CustomerDto> findAll() {
        List<Customer> customers = customerDao.findAll();
        return customers.stream()
                .map(customerConverter::convert)
                .collect(Collectors.toList());
    }

    public CustomerDto getById(Long id) {
        Customer customer = Optional.ofNullable(customerDao.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Customer with id %d not found", id), ErrorCodes.CUSTOMER_NOT_FOUND));
        return customerConverter.convert(customer);
    }

    public CustomerDto create(CustomerCreateDto customerCreateDto) {
        Customer customer = new Customer();
        customer.setFirstName(customerCreateDto.getFirstName());
        customer.setLastName(customerCreateDto.getLastName());
        customer.setDateOfBirth(customerCreateDto.getDateOfBirth());
        customerDao.insert(customer);
        return customerConverter.convert(customer);
    }

}
