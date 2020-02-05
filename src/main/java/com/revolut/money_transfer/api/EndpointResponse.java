package com.revolut.money_transfer.api;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class EndpointResponse {

    private final boolean success;
    private final Integer httpStatusCode;
    private final LocalDateTime date;

    public EndpointResponse(boolean success, Integer httpStatusCode, LocalDateTime date) {
        this.success = success;
        this.httpStatusCode = httpStatusCode;
        this.date = date;
    }

    public static EndpointResponse ok() {
        return new EndpointResponse(true, 200, LocalDateTime.now(ZoneOffset.UTC));
    }
}
