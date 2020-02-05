package com.revolut.money_transfer.api.exception;

import java.util.Map;

public class BadRequestException extends ApiException {

    public static final int HTTP_STATUS_CODE_400 = 400;

    public BadRequestException(String message, ErrorCodes errorCode) {
        super(message, HTTP_STATUS_CODE_400, errorCode.getErrorCode());
    }

    public BadRequestException(String message, ErrorCodes errorCode, Map<String, String> validationErrors) {
        super(HTTP_STATUS_CODE_400, new ValidationExceptionPayload(HTTP_STATUS_CODE_400, message, errorCode.getErrorCode(), validationErrors));
    }

    static class ValidationExceptionPayload extends ExceptionPayload {
        private final Map<String, String> validationErrors;

        ValidationExceptionPayload(Integer httpStatusCode, String message, Integer errorCode, Map<String, String> validationErrors) {
            super(httpStatusCode, message, errorCode);
            this.validationErrors = validationErrors;
        }

        public Map<String, String> getValidationErrors() {
            return validationErrors;
        }
    }

    @Override
    public ExceptionPayload getPayload() {
        return super.getPayload();
    }
}
