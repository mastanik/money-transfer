package com.revolut.money_transfer.bank;

import com.revolut.money_transfer.bank.exception.DifferentCurrenciesException;
import com.revolut.money_transfer.bank.exception.MoneyValueTooBigException;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MoneyTest {

    @Test
    public void test_MoneyRounding() {
        Money scaledDown = new Money(new BigDecimal("12345.1234"), "USD");
        Money scaledUp = new Money(new BigDecimal("12345.1235"), "USD");

        Money moneyScaleFourRoundDown = new Money(new BigDecimal("12345.1234"), "USD");
        assertEquals(scaledDown, moneyScaleFourRoundDown);

        Money moneyScaleFiveRoundUp = new Money(new BigDecimal("12345.12346"), "USD");
        assertEquals(scaledUp, moneyScaleFiveRoundUp);

        Money moneyScaleFiveRoundDown = new Money(new BigDecimal("12345.12345"), "USD");
        assertEquals(scaledDown, moneyScaleFiveRoundDown);

        Money moneyScaleSixRoundUp = new Money(new BigDecimal("12345.123451"), "USD");
        assertEquals(scaledUp, moneyScaleSixRoundUp);

        Money moneyScaleSixRoundDown = new Money(new BigDecimal("12345.123450"), "USD");
        assertEquals(scaledDown, moneyScaleSixRoundDown);
    }

    @Test
    public void test_MoneyFormat_Ok() {
        Money money19digits = new Money(new BigDecimal("1234567890123456789.1234"), "USD");
        assertEquals(new Money(new BigDecimal("1234567890123456789.1234"), "USD"), money19digits);

        Money money19digits19Scale = new Money(new BigDecimal("1234567890123456789.1234567890123456789"), "USD");
        assertEquals(new Money(new BigDecimal("1234567890123456789.1235"), "USD"), money19digits19Scale);
    }

    @Test(expected = MoneyValueTooBigException.class)
    public void test_MoneyFormat_NotSupported() {
        new Money(new BigDecimal("12345678901234567890.12"), "USD");
    }

    @Test
    public void test_Add() {
        Money dollar = new Money(new BigDecimal("1"), "USD");
        Money oneMoredollar = new Money(new BigDecimal("1"), "USD");
        Money twoDollars = dollar.add(oneMoredollar);
        assertEquals(new Money(new BigDecimal("2"), "USD"), twoDollars);

        Money minusOneDollar = new Money(new BigDecimal("-1"), "USD");
        Money zeroDollars = dollar.add(minusOneDollar);
        assertEquals(new Money(new BigDecimal("0"), "USD"), zeroDollars);

        dollar = new Money(new BigDecimal("1.5005"), "USD");
        oneMoredollar = new Money(new BigDecimal("1.4995"), "USD");
        Money threeDollars = dollar.add(oneMoredollar);
        assertEquals(new Money(new BigDecimal("3"), "USD"), threeDollars);

        dollar = new Money(new BigDecimal("1.5005"), "USD");
        oneMoredollar = new Money(new BigDecimal("1.9999"), "USD");
        Money threeDollarsWithScale = dollar.add(oneMoredollar);
        assertEquals(new Money(new BigDecimal("3.5004"), "USD"), threeDollarsWithScale);
    }

    @Test(expected = DifferentCurrenciesException.class)
    public void test_Add_DifferentCurrencies() {
        Money dollar = new Money(new BigDecimal("1"), "USD");
        Money euro = new Money(new BigDecimal("1"), "EUR");
        dollar.add(euro);
    }

    @Test(expected = MoneyValueTooBigException.class)
    public void test_Add_TooBigValue() {
        Money maxDollars = new Money(new BigDecimal("9999999999999999999.9999"), "USD");
        Money oneDollar = new Money(new BigDecimal("1"), "USD");
        maxDollars.add(oneDollar);
    }

    @Test
    public void test_Subtract() {
        Money dollar = new Money(new BigDecimal("1"), "USD");
        Money oneMoredollar = new Money(new BigDecimal("1"), "USD");
        Money zeroDollars = dollar.subtract(oneMoredollar);
        assertEquals(new Money(new BigDecimal("0"), "USD"), zeroDollars);

        Money twoDollars = new Money(new BigDecimal("2"), "USD");
        Money minusOneDollar = dollar.subtract(twoDollars);
        assertEquals(new Money(new BigDecimal("-1"), "USD"), minusOneDollar);

        Money oneCent = new Money(new BigDecimal("0.01"), "USD");
        Money cents99 = dollar.subtract(oneCent);
        assertEquals(new Money(new BigDecimal("0.99"), "USD"), cents99);
    }

    @Test(expected = MoneyValueTooBigException.class)
    public void test_Subtract_TooBigValue() {
        Money maxDollars = new Money(new BigDecimal("-9999999999999999999.9999"), "USD");
        Money oneDollar = new Money(new BigDecimal("1"), "USD");
        maxDollars.subtract(oneDollar);
    }

    @Test(expected = DifferentCurrenciesException.class)
    public void test_Subtract_DifferentCurrencies() {
        Money dollar = new Money(new BigDecimal("1"), "USD");
        Money euro = new Money(new BigDecimal("1"), "EUR");
        dollar.subtract(euro);
    }

    @Test
    public void test_Multiply() {
        Money dollar = new Money(new BigDecimal("1"), "USD");
        Money dollarMultiplied = dollar.multiply(dollar.getAmount());
        assertEquals(dollar, dollarMultiplied);

        BigDecimal value1 = new BigDecimal("12345.6789");
        BigDecimal value2 = new BigDecimal("1.2345");
        BigDecimal expected = value1.multiply(value2).setScale(4, RoundingMode.HALF_EVEN);

        Money dollars = new Money(value1, "USD");
        Money multiplied = dollars.multiply(value2);
        assertEquals(new Money(expected, "USD"), multiplied);
    }

    @Test(expected = MoneyValueTooBigException.class)
    public void test_Multiply_TooBigValue() {
        Money maxDollars = new Money(new BigDecimal("9999999999999999999.9999"), "USD");
        maxDollars.multiply(new BigDecimal("2"));
    }

    @Test
    public void test_Equals() {
        Money dollar1 = new Money(new BigDecimal("1"), "USD");
        Money dollar2 = new Money(new BigDecimal("1"), "USD");
        Money euro = new Money(new BigDecimal("1"), "EUR");

        assertEquals(dollar1, dollar2);
        assertNotEquals(dollar1, euro);
    }

}
