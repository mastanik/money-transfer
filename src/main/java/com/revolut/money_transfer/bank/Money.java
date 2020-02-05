package com.revolut.money_transfer.bank;

import com.revolut.money_transfer.api.exception.ErrorCodes;
import com.revolut.money_transfer.bank.exception.DifferentCurrenciesException;
import com.revolut.money_transfer.bank.exception.MoneyValueTooBigException;

import java.math.BigDecimal;
import java.util.Objects;

import static java.math.BigDecimal.ROUND_HALF_EVEN;

public class Money {

    public static final int SCALE = 4;
    public static final int ROUNDING_MODE = ROUND_HALF_EVEN;
    public static final int MAX_INTEGER_PART = 19;

    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
        if (this.amount.precision() - SCALE > MAX_INTEGER_PART) {
            throw new MoneyValueTooBigException("Money value is too big", ErrorCodes.MONEY_VALUE_TOO_BIG);
        }
        this.currency = currency;
    }

    public Money add(Money money) {
        checkSameCurrency(money);
        return new Money(this.amount.add(money.getAmount()), money.getCurrency());
    }

    public Money subtract(Money money) {
        checkSameCurrency(money);
        return new Money(this.amount.subtract(money.getAmount()), money.getCurrency());
    }

    Money multiply(BigDecimal amount) {
        return new Money(this.amount.multiply(amount), this.currency);
    }

    private void checkSameCurrency(Money money) {
        if (!this.currency.equals(money.currency)) {
            throw new DifferentCurrenciesException("Can  not operate on different currencies", ErrorCodes.DIFFERENT_CURRENCIES);
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.equals(money.amount) &&
                currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return "Money{" +
                "amount=" + amount +
                ", currency='" + currency + '\'' +
                '}';
    }
}
