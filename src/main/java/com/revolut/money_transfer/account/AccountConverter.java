package com.revolut.money_transfer.account;

import com.revolut.money_transfer.account.dto.AccountDto;
import org.jooq.codegen.revolut.tables.pojos.Account;

public class AccountConverter {

    public AccountDto convert(Account account) {
        return new AccountDto.AccountDtoBuilder()
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .customerId(account.getCustomerId())
                .id(account.getId())
                .build();
    }

}
