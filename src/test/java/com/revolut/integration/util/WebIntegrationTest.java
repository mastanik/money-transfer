package com.revolut.integration.util;

import com.google.gson.Gson;
import com.revolut.money_transfer.TestHttpClient;
import com.revolut.money_transfer.api.configuration.GsonConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

public class WebIntegrationTest extends DbIntegrationTest {

    public static final String BASE_URL = "http://localhost:4567";

    protected TestHttpClient testHttpClient = new TestHttpClient();
    protected Gson gson = new GsonConfiguration().get();

    @BeforeClass
    public static void setup() {
        DbIntegrationTest.setup();

        app.init();
        awaitInitialization();
    }

    @AfterClass
    public static void tearDown() {
        stop();
    }
}
