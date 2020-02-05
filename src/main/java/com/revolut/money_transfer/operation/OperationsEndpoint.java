package com.revolut.money_transfer.operation;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.revolut.money_transfer.api.BaseEndpoint;
import com.revolut.money_transfer.api.EndpointResponse;
import com.revolut.money_transfer.operation.dto.DepositDto;
import com.revolut.money_transfer.operation.dto.TransferDto;
import com.revolut.money_transfer.operation.dto.WithdrawDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

public class OperationsEndpoint extends BaseEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(OperationsEndpoint.class);
    private final OperationsService operationsService;

    @Inject
    public OperationsEndpoint(OperationsService operationsService, Gson gson) {
        super(gson);
        this.operationsService = operationsService;
    }

    public EndpointResponse deposit(Request request) {
        DepositDto depositDto = getValidObject(request, DepositDto.class);
        logger.debug("Deposit operation {}", depositDto);
        operationsService.deposit(depositDto);
        return EndpointResponse.ok();
    }

    public EndpointResponse withdraw(Request request) {
        WithdrawDto withdrawDto = getValidObject(request, WithdrawDto.class);
        logger.debug("Withdraw operation {}", withdrawDto);
        operationsService.withdraw(withdrawDto);
        return EndpointResponse.ok();
    }

    public EndpointResponse transfer(Request request) {
        TransferDto transferDto = getValidObject(request, TransferDto.class);
        logger.debug("Transfer operation {}", transferDto);
        operationsService.transfer(transferDto);
        return EndpointResponse.ok();
    }
}
