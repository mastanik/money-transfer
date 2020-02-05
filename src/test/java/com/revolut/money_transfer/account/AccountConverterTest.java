package com.revolut.money_transfer.account;

import com.revolut.money_transfer.account.dto.AccountDto;
import org.jooq.codegen.revolut.tables.pojos.Account;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class AccountConverterTest {

    private final AccountConverter accountConverter = new AccountConverter();

    @Test
    public void test_Convert() {
        AccountDto expected = new AccountDto.AccountDtoBuilder()
                .id(1L)
                .customerId(2L)
                .currency("USD")
                .balance(new BigDecimal("100.50"))
                .build();

        Account account = new Account();
        account.setId(1L);
        account.setCustomerId(2L);
        account.setCurrency("USD");
        account.setBalance(new BigDecimal("100.50"));

        AccountDto dto = accountConverter.convert(account);

        assertEquals(expected.getId(), dto.getId());
        assertEquals(expected.getCurrency(), dto.getCurrency());
        assertEquals(expected.getBalance(), dto.getBalance());
        assertEquals(expected.getCustomerId(), dto.getCustomerId());

    }
}
