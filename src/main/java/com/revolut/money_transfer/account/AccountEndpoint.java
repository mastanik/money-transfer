package com.revolut.money_transfer.account;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.revolut.money_transfer.account.dto.AccountCreateDto;
import com.revolut.money_transfer.account.dto.AccountDto;
import com.revolut.money_transfer.api.BaseEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.util.List;

public class AccountEndpoint extends BaseEndpoint {

    private static Logger logger = LoggerFactory.getLogger(AccountEndpoint.class);
    private final AccountService accountService;

    @Inject
    public AccountEndpoint(AccountService accountService, Gson gson) {
        super(gson);
        this.accountService = accountService;
    }

    public List<AccountDto> findAll() {
        logger.debug("Account Find All {}", this);
        return accountService.findAll();
    }

    public AccountDto getById(Request request) {
        Long id = getIdFromRequest(request);
        logger.debug("Account Get By Id {}", id);
        return accountService.getById(id);
    }

    public AccountDto create(Request request) {
        AccountCreateDto accountCreateDto = getValidObject(request, AccountCreateDto.class);
        logger.debug("Account Create {}", accountCreateDto);
        return accountService.create(accountCreateDto);
    }
}
