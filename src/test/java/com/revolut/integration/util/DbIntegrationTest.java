package com.revolut.integration.util;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.revolut.App;
import com.revolut.money_transfer.api.configuration.EnvironmentDataSourceConfiguration;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jooq.codegen.revolut.tables.daos.AccountDao;
import org.jooq.codegen.revolut.tables.daos.CustomerDao;
import org.jooq.codegen.revolut.tables.pojos.Account;
import org.jooq.codegen.revolut.tables.pojos.Customer;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.math.BigDecimal;
import java.util.UUID;

public class DbIntegrationTest {

    private static Flyway flyway;
    protected static Injector injector;
    protected static App app;

    @BeforeClass
    public static void setup() {
        System.setProperty("jdbcUrl", "jdbc:h2:~/revolut-db");
        System.setProperty("db_user", "revolut-user");
        System.setProperty("db_password", "revolut-password");

        FluentConfiguration fluentConfiguration = new FluentConfiguration();
        fluentConfiguration.dataSource(new EnvironmentDataSourceConfiguration().get());
        fluentConfiguration.locations("filesystem:src/main/resources/db/migration");
        fluentConfiguration.baselineOnMigrate(true);
        flyway = new Flyway(fluentConfiguration);
        flyway.migrate();

        injector = Guice.createInjector(new MoneyTransferModuleTest());
        app = injector.getInstance(App.class);
    }

    protected Customer createRandomCustomer() {
        CustomerDao customerDao = injector.getInstance(CustomerDao.class);
        Customer customer = new Customer();
        customer.setLastName(UUID.randomUUID().toString());
        customer.setFirstName(UUID.randomUUID().toString());
        customer.setDateOfBirth("1999-09-09");
        customerDao.insert(customer);
        return customerDao.findById(customerDao.getId(customer));
    }

    protected Account createRandomAccount(Customer customer, String currency, BigDecimal balance) {
        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account account = new Account();
        account.setVersion(1L);
        account.setBalance(balance);
        account.setCurrency(currency);
        account.setCustomerId(customer.getId());
        accountDao.insert(account);
        return accountDao.findById(accountDao.getId(account));
    }

    @AfterClass
    public static void tearDown() {
        flyway.clean();
    }
}
