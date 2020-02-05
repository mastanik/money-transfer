package com.revolut.money_transfer.api.exception;

public class ServerErrorException extends ApiException {
    public ServerErrorException(String message, ErrorCodes errorCode) {
        super(message, 500, errorCode.getErrorCode());
    }
}
