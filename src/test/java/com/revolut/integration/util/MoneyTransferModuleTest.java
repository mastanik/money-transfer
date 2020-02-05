package com.revolut.integration.util;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.revolut.money_transfer.api.configuration.EnvironmentDataSourceConfiguration;
import com.revolut.money_transfer.api.configuration.GsonConfiguration;
import com.revolut.money_transfer.api.configuration.JooqDaoConfiguration;
import com.revolut.money_transfer.operation.OperationsService;
import com.revolut.money_transfer.operation.TransactionalOperationsService;
import org.jooq.Configuration;
import org.jooq.codegen.revolut.tables.daos.AccountDao;
import org.jooq.codegen.revolut.tables.daos.CurrencyExchangeRateDao;
import org.jooq.codegen.revolut.tables.daos.CustomerDao;
import org.jooq.codegen.revolut.tables.daos.SupportedCurrenciesDao;

import javax.sql.DataSource;

public class MoneyTransferModuleTest extends AbstractModule {

    @Override
    protected void configure() {
        bind(DataSource.class).toProvider(EnvironmentDataSourceConfiguration.class).in(Scopes.SINGLETON);
        bind(Configuration.class).toProvider(JooqDaoConfiguration.class).in(Scopes.SINGLETON);
        bind(Gson.class).toProvider(GsonConfiguration.class).in(Scopes.SINGLETON);
        bind(OperationsService.class).to(TransactionalOperationsService.class).in(Scopes.SINGLETON);

        try {
            bind(CustomerDao.class).toConstructor(
                    CustomerDao.class.getConstructor(Configuration.class));
            bind(AccountDao.class).toConstructor(
                    AccountDao.class.getConstructor(Configuration.class));
            bind(SupportedCurrenciesDao.class).toConstructor(
                    SupportedCurrenciesDao.class.getConstructor(Configuration.class));
            bind(CurrencyExchangeRateDao.class).toConstructor(
                    CurrencyExchangeRateDao.class.getConstructor(Configuration.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

}
