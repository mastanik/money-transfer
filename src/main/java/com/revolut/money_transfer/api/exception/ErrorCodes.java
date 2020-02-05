package com.revolut.money_transfer.api.exception;

public enum ErrorCodes {
    CUSTOMER_NOT_FOUND(1_000, "Customer not found"),

    ACCOUNT_NOT_FOUND(2_000, "Account not found"),

    INSUFFICIENT_FUNDS(3_000, "Not enough funds for withdrawal"),

    NO_EXCHANGE_RATE(4_000, "There is no exchange rate for currency pair"),
    DIFFERENT_CURRENCIES(4_001, "Can not operate on different currency types"),
    MONEY_VALUE_TOO_BIG(4_002, "Money value is too big"),
    CURRENCY_NOT_SUPPORTED(4_003, "Currency not supported"),

    VALIDATION_ERROR_JSON_SYNTAX(9_996, "Can not deserialize string payload"),
    VALIDATION_ERROR(9_997, "Validation failed"),
    BAD_RESOURCE_ID_TYPE(9_998, "Bad type of resource id field"),
    INTERNAL_SERVER_ERROR(9_999, "Internal server error, please report to developers");

    private final Integer errorCode;
    private final String errorMessage;

    ErrorCodes(Integer errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
