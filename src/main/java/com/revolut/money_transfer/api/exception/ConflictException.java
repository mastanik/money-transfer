package com.revolut.money_transfer.api.exception;

public class ConflictException extends ApiException {
    public ConflictException(String message, ErrorCodes errorCode) {
        super(message, 409, errorCode.getErrorCode());
    }
}
