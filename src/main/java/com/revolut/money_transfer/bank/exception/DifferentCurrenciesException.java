package com.revolut.money_transfer.bank.exception;

import com.revolut.money_transfer.api.exception.ConflictException;
import com.revolut.money_transfer.api.exception.ErrorCodes;

public class DifferentCurrenciesException extends ConflictException {
    public DifferentCurrenciesException(String message, ErrorCodes errorCode) {
        super(message, errorCode);
    }
}
