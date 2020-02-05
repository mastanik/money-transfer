package com.revolut.money_transfer.customer;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.revolut.money_transfer.api.BaseEndpoint;
import com.revolut.money_transfer.customer.dto.CustomerCreateDto;
import com.revolut.money_transfer.customer.dto.CustomerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.util.List;

public class CustomerEndpoint extends BaseEndpoint {

    private static Logger logger = LoggerFactory.getLogger(CustomerEndpoint.class);
    private final CustomerService customerService;

    @Inject
    public CustomerEndpoint(CustomerService customerService, Gson gson) {
        super(gson);
        this.customerService = customerService;
    }

    public List<CustomerDto> findAll() {
        logger.debug("Customer Find All");
        return customerService.findAll();
    }

    public CustomerDto getById(Request request) {
        Long id = getIdFromRequest(request);
        logger.debug("Customer Get By Id {}", id);
        return customerService.getById(id);
    }

    public CustomerDto create(Request request) {
        CustomerCreateDto customerCreateDto = getValidObject(request, CustomerCreateDto.class);
        logger.debug("Customer Create {}", customerCreateDto);
        return customerService.create(customerCreateDto);
    }
}
