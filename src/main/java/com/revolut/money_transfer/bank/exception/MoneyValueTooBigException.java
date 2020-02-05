package com.revolut.money_transfer.bank.exception;

import com.revolut.money_transfer.api.exception.ErrorCodes;
import com.revolut.money_transfer.api.exception.ServerErrorException;

public class MoneyValueTooBigException extends ServerErrorException {
    public MoneyValueTooBigException(String message, ErrorCodes errorCode) {
        super(message, errorCode);
    }
}
