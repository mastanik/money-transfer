package com.revolut.money_transfer.api.exception;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ApiException extends RuntimeException {

    private final Integer httpStatusCode;
    private final ExceptionPayload payload;

    public static ApiException getGenericServerErrorPayload() {
        return new ApiException("Internal Server Error", 503, ErrorCodes.INTERNAL_SERVER_ERROR.getErrorCode());
    }

    ApiException(String message, Integer httpStatusCode, Integer errorCode) {
        super();
        this.httpStatusCode = httpStatusCode;
        this.payload = new ExceptionPayload(httpStatusCode, message, errorCode);
    }

    ApiException(Integer httpStatusCode, ExceptionPayload exceptionPayload) {
        super();
        this.httpStatusCode = httpStatusCode;
        this.payload = exceptionPayload;
    }

    static class ExceptionPayload {
        private final Integer httpStatusCode;
        private final LocalDateTime date;
        private final String message;
        private final Integer errorCode;

        ExceptionPayload(Integer httpStatusCode, String message, Integer errorCode) {
            this.httpStatusCode = httpStatusCode;
            this.date = LocalDateTime.now(ZoneOffset.UTC);
            this.message = message;
            this.errorCode = errorCode;
        }

        public Integer getHttpStatusCode() {
            return httpStatusCode;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public String getMessage() {
            return message;
        }

        public Integer getErrorCode() {
            return errorCode;
        }
    }

    public ExceptionPayload getPayload() {
        return payload;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }
}
