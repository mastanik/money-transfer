package com.revolut.money_transfer.operation.dto;

import com.revolut.money_transfer.api.validation.BigDecimalFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TransferDto {
    @NotNull
    private final Long accountIdFrom;
    @NotNull
    private final Long accountIdTo;
    @BigDecimalFormat
    private final BigDecimal amount;
    @NotNull
    private final String currency;

    public TransferDto(Long accountIdFrom, Long accountIdTo, BigDecimal amount, String currency) {
        this.accountIdFrom = accountIdFrom;
        this.accountIdTo = accountIdTo;
        this.amount = amount;
        this.currency = currency;
    }

    public Long getAccountIdFrom() {
        return accountIdFrom;
    }

    public Long getAccountIdTo() {
        return accountIdTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return "TransferDto{" +
                "accountIdFrom=" + accountIdFrom +
                ", accountIdTo=" + accountIdTo +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                '}';
    }
}
