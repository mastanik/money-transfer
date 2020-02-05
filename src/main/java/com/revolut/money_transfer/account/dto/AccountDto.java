package com.revolut.money_transfer.account.dto;

import java.math.BigDecimal;

public class AccountDto {
    private final Long id;
    private final Long customerId;
    private final String currency;
    private final BigDecimal balance;

    private AccountDto(Long id, Long customerId, String currency, BigDecimal balance) {
        this.id = id;
        this.customerId = customerId;
        this.currency = currency;
        this.balance = balance;
    }

    public static class AccountDtoBuilder {
        private Long id;
        private Long customerId;
        private String currency;
        private BigDecimal balance;

        public AccountDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AccountDtoBuilder customerId(Long customerId) {
            this.customerId = customerId;
            return this;
        }

        public AccountDtoBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public AccountDtoBuilder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public AccountDto build() {
            return new AccountDto(id, customerId, currency, balance);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
