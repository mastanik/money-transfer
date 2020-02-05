package com.revolut.money_transfer.account.dto;

import javax.validation.constraints.NotNull;

public class AccountCreateDto {
    @NotNull
    private final Long customerId;
    @NotNull
    private final String currency;

    public AccountCreateDto(Long customerId, String currency) {
        this.customerId = customerId;
        this.currency = currency;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return "AccountCreateDto{" +
                "customerId=" + customerId +
                ", currency='" + currency + '\'' +
                '}';
    }
}
