package com.revolut.money_transfer.account.exception;

import com.revolut.money_transfer.api.exception.BadRequestException;
import com.revolut.money_transfer.api.exception.ErrorCodes;

public class CurrencyNotSupportedException extends BadRequestException {
    public CurrencyNotSupportedException(String message, ErrorCodes errorCode) {
        super(message, errorCode);
    }
}
