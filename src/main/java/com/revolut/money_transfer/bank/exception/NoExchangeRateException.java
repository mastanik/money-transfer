package com.revolut.money_transfer.bank.exception;

import com.revolut.money_transfer.api.exception.ConflictException;
import com.revolut.money_transfer.api.exception.ErrorCodes;

public class NoExchangeRateException extends ConflictException {

    public NoExchangeRateException(String message, ErrorCodes errorCode) {
        super(message, errorCode);
    }
}
