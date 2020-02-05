package com.revolut.money_transfer.api.validation;

import com.revolut.money_transfer.bank.Money;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class BigDecimalValidator implements ConstraintValidator<BigDecimalFormat, BigDecimal> {
    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        return value != null && (value.scale() <= Money.SCALE) && (value.precision() - value.scale() <= Money.MAX_INTEGER_PART);
    }
}
