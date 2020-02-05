package com.revolut;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.revolut.money_transfer.account.AccountEndpoint;
import com.revolut.money_transfer.api.configuration.MoneyTransferModule;
import com.revolut.money_transfer.api.exception.ApiException;
import com.revolut.money_transfer.customer.CustomerEndpoint;
import com.revolut.money_transfer.operation.OperationsEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;

public class App {

    private final AccountEndpoint accountEndpoint;
    private final CustomerEndpoint customerEndpoint;
    private final OperationsEndpoint operationsEndpoint;
    private final Gson gson;

    private static Logger logger = LoggerFactory.getLogger(App.class);

    @Inject
    public App(AccountEndpoint accountEndpoint, CustomerEndpoint customerEndpoint, OperationsEndpoint operationsEndpoint, Gson gson) {
        this.accountEndpoint = accountEndpoint;
        this.customerEndpoint = customerEndpoint;
        this.operationsEndpoint = operationsEndpoint;
        this.gson = gson;
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new MoneyTransferModule());
        App app = injector.getInstance(App.class);
        app.init();
    }

    public void init() {

        before((request, response) -> response.type("application/json"));

        get("/accounts", (req, res) -> accountEndpoint.findAll(), gson::toJson);
        get("/account/:id", (req, res) -> accountEndpoint.getById(req), gson::toJson);
        post("/account", (req, res) -> accountEndpoint.create(req), gson::toJson);

        get("/customers", (req, res) -> customerEndpoint.findAll(), gson::toJson);
        get("/customer/:id", (req, res) -> customerEndpoint.getById(req), gson::toJson);
        post("/customer", (req, res) -> customerEndpoint.create(req), gson::toJson);

        post("/operation/deposit", (req, res) -> operationsEndpoint.deposit(req), gson::toJson);
        post("/operation/withdraw", (req, res) -> operationsEndpoint.withdraw(req), gson::toJson);
        post("/operation/transfer", (req, res) -> operationsEndpoint.transfer(req), gson::toJson);

        exception(ApiException.class, (exception, request, response) -> {
            logger.error("Api exception occurred", exception);
            response.status(exception.getHttpStatusCode());
            response.body(gson.toJson(exception.getPayload()));
        });

        exception(Exception.class, (exception, request, response) -> {
            logger.error("Exception occurred", exception);
            ApiException internalException = ApiException.getGenericServerErrorPayload();
            response.status(internalException.getHttpStatusCode());
            response.body(gson.toJson(internalException.getPayload()));
        });
    }

}
