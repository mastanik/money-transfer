package com.revolut.money_transfer.api.configuration;

import com.google.inject.Provider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class EnvironmentDataSourceConfiguration implements Provider<DataSource> {

    private static final String JDBC_URL = "jdbcUrl";
    private static final String DB_USERNAME = "db_user";
    private static final String DB_PASSWORD = "db_password";

    @Override
    public DataSource get() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getFromPropertiesOrEnvironment(JDBC_URL));
        config.setUsername(getFromPropertiesOrEnvironment(DB_USERNAME));
        config.setPassword(getFromPropertiesOrEnvironment(DB_PASSWORD));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return new HikariDataSource(config);
    }

    private String getFromPropertiesOrEnvironment(String key) {
        return System.getProperty(key) != null ? System.getProperty(key) : System.getenv(key);
    }
}
