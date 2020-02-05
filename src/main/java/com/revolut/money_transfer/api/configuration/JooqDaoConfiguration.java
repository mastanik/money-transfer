package com.revolut.money_transfer.api.configuration;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.jooq.Configuration;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultConfiguration;

import javax.sql.DataSource;

public class JooqDaoConfiguration implements Provider<Configuration> {

    @Inject
    private DataSource dataSource;

    @Override
    public Configuration get() {
        return new DefaultConfiguration()
                .set(new Settings()
                        .withExecuteWithOptimisticLocking(true)
                        .withExecuteWithOptimisticLockingExcludeUnversioned(true))
                .set(dataSource);
    }
}
