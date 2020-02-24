package com.revolut.money_transfer.bank;

import com.google.inject.Inject;
import com.revolut.money_transfer.api.exception.ErrorCodes;
import com.revolut.money_transfer.bank.exception.NoExchangeRateException;
import org.jooq.codegen.revolut.tables.daos.CurrencyExchangeRateDao;
import org.jooq.codegen.revolut.tables.daos.SupportedCurrenciesDao;
import org.jooq.codegen.revolut.tables.pojos.SupportedCurrencies;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BankService {

    private final SupportedCurrenciesDao supportedCurrenciesDao;
    private final CurrencyExchangeRateDao currencyExchangeRateDao;
    private final Set<String> supportedCurrencies;
    private final Map<ExchangeRateKey, BigDecimal> exchangeRate;

    @Inject
    public BankService(SupportedCurrenciesDao supportedCurrenciesDao, CurrencyExchangeRateDao currencyExchangeRateDao) {
        this.supportedCurrenciesDao = supportedCurrenciesDao;
        this.currencyExchangeRateDao = currencyExchangeRateDao;
        this.supportedCurrencies = new HashSet<>();
        this.exchangeRate = new HashMap<>();
        init();
    }

    private class ExchangeRateKey {
        private final String sourceCurrency;
        private final String targetCurrency;

        ExchangeRateKey(String sourceCurrency, String targetCurrency) {
            this.sourceCurrency = sourceCurrency;
            this.targetCurrency = targetCurrency;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExchangeRateKey that = (ExchangeRateKey) o;
            return sourceCurrency.equals(that.sourceCurrency) &&
                    targetCurrency.equals(that.targetCurrency);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceCurrency, targetCurrency);
        }
    }

    private void init() {
        this.supportedCurrenciesDao.findAll()
                .stream()
                .map(SupportedCurrencies::getCurrency)
                .collect(Collectors.toCollection(() -> supportedCurrencies));

        this.currencyExchangeRateDao.findAll()
                .forEach(currencyExchangeRate -> this.exchangeRate.put(
                        new ExchangeRateKey(currencyExchangeRate.getSourceCurrency(), currencyExchangeRate.getTargetCurrency()),
                        currencyExchangeRate.getExchangeRate()));
    }

    public boolean isCurrencySupported(String currency) {
        return supportedCurrencies.contains(currency);
    }

    public Money exchange(Money money, String toCurrency) {
        if (money.getCurrency().equals(toCurrency)) {
            return money;
        }
        BigDecimal rate = this.exchangeRate.get(new ExchangeRateKey(money.getCurrency(), toCurrency));
        if (rate == null) {
            throw new NoExchangeRateException(String.format("No rates for the currencies %s -> %s", money.getCurrency(), toCurrency), ErrorCodes.NO_EXCHANGE_RATE);
        }
        Money exchangedAmount = money.multiply(rate);
        return new Money(exchangedAmount.getAmount(), toCurrency);
    }
}
