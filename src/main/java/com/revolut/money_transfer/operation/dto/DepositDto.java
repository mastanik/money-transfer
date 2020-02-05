package com.revolut.money_transfer.operation.dto;

import com.revolut.money_transfer.api.validation.BigDecimalFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class DepositDto {
    @NotNull
    private final Long accountId;
    @BigDecimalFormat
    private final BigDecimal amount;
    @NotNull
    private final String currency;

    public DepositDto(Long accountId, BigDecimal amount, String currency) {
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
    }

    public Long getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return "DepositDto{" +
                "accountId=" + accountId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                '}';
    }
}
