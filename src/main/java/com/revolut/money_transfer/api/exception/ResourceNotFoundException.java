package com.revolut.money_transfer.api.exception;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message, ErrorCodes errorCode) {
        super(message, 404, errorCode.getErrorCode());
    }

}
