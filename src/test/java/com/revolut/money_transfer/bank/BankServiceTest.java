package com.revolut.money_transfer.bank;

import com.google.common.collect.Lists;
import com.revolut.money_transfer.bank.exception.MoneyValueTooBigException;
import com.revolut.money_transfer.bank.exception.NoExchangeRateException;
import org.jooq.codegen.revolut.tables.daos.CurrencyExchangeRateDao;
import org.jooq.codegen.revolut.tables.daos.SupportedCurrenciesDao;
import org.jooq.codegen.revolut.tables.pojos.CurrencyExchangeRate;
import org.jooq.codegen.revolut.tables.pojos.SupportedCurrencies;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BankServiceTest {

    @Mock
    private SupportedCurrenciesDao supportedCurrenciesDao;
    @Mock
    private CurrencyExchangeRateDao currencyExchangeRateDao;
    private BankService bankService;

    private List<SupportedCurrencies> usdEurGbpCurrencies = Lists.newArrayList(
            new SupportedCurrencies(1L, "USD"),
            new SupportedCurrencies(2L, "EUR"),
            new SupportedCurrencies(3L, "GBP")
    );

    private List<CurrencyExchangeRate> currencyExchangeRates = Lists.newArrayList(
            new CurrencyExchangeRate(1L, "EUR", "USD", new BigDecimal("1.1100")),
            new CurrencyExchangeRate(2L, "GBP", "USD", new BigDecimal("1.3000")),
            new CurrencyExchangeRate(3L, "USD", "GBP", new BigDecimal("0.7700"))
    );

    @Before
    public void setup() {
        when(supportedCurrenciesDao.findAll()).thenReturn(usdEurGbpCurrencies);
        when(currencyExchangeRateDao.findAll()).thenReturn(currencyExchangeRates);
        bankService = new BankService(supportedCurrenciesDao, currencyExchangeRateDao);
    }

    @Test
    public void isCurrencySupported_Ok() {
        assertTrue(bankService.isCurrencySupported("USD"));
        assertTrue(bankService.isCurrencySupported("EUR"));
        assertTrue(bankService.isCurrencySupported("GBP"));
    }

    @Test
    public void isCurrencySupported_False() {
        assertFalse(bankService.isCurrencySupported("MXN"));
    }

    @Test
    public void exchange_SameCurrency() {
        Money dollars = new Money(new BigDecimal("5"), "USD");
        Money exchangedDollars = bankService.exchange(dollars, "USD");
        assertEquals(dollars, exchangedDollars);
    }

    @Test(expected = NoExchangeRateException.class)
    public void exchange_NotExistingRate() {
        Money dollars = new Money(new BigDecimal("5"), "USD");
        bankService.exchange(dollars, "MXN");
    }

    @Test
    public void exchange_One_ExistingRate() {
        Money euro = new Money(new BigDecimal("1"), "EUR");
        Money exchangedEuro = bankService.exchange(euro, "USD");

        Money gbp = new Money(new BigDecimal("1"), "GBP");
        Money exchangedGbp = bankService.exchange(gbp, "USD");

        Money dollar = new Money(new BigDecimal("1"), "USD");
        Money exchangedDollar = bankService.exchange(dollar, "GBP");

        assertEquals(new Money(new BigDecimal("1.1100"), "USD"), exchangedEuro);
        assertEquals(new Money(new BigDecimal("1.3000"), "USD"), exchangedGbp);
        assertEquals(new Money(new BigDecimal("0.7700"), "GBP"), exchangedDollar);
    }

    @Test
    public void exchange_BigAmount_ExistingRate() {
        BigDecimal big = new BigDecimal("1234567890123456789.1234");

        BigDecimal exchangeRate = currencyExchangeRates.stream()
                .filter(cer -> cer.getSourceCurrency().equals("USD") && cer.getTargetCurrency().equals("GBP"))
                .findFirst()
                .get()
                .getExchangeRate();

        BigDecimal expected = big.multiply(exchangeRate).setScale(4, RoundingMode.HALF_EVEN);

        Money aLotOfDollars = new Money(big, "USD");
        Money exchanged = bankService.exchange(aLotOfDollars, "GBP");
        assertEquals(new Money(expected, "GBP"), exchanged);
    }

    @Test(expected = MoneyValueTooBigException.class)
    public void exchange_TooBigAmount() {
        BigDecimal big = new BigDecimal("9999999999999999999.1234");

        Money aLotOfGbp = new Money(big, "GBP");
        bankService.exchange(aLotOfGbp, "USD");
    }

}
