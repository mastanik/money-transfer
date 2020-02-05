package com.revolut.money_transfer.account;

import com.google.common.collect.Lists;
import com.revolut.money_transfer.account.dto.AccountCreateDto;
import com.revolut.money_transfer.account.dto.AccountDto;
import com.revolut.money_transfer.account.exception.CurrencyNotSupportedException;
import com.revolut.money_transfer.api.exception.ResourceNotFoundException;
import com.revolut.money_transfer.bank.BankService;
import com.revolut.money_transfer.customer.CustomerService;
import com.revolut.money_transfer.customer.dto.CustomerDto;
import org.jooq.codegen.revolut.tables.daos.AccountDao;
import org.jooq.codegen.revolut.tables.pojos.Account;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {

    @Mock
    private AccountDao accountDao;
    @Mock
    private CustomerService customerService;
    @Mock
    private BankService bankService;

    private AccountService accountService;

    private Account usdAccount = new Account(1L, 1L, "USD", new BigDecimal("50.10"), 1L);
    private Account createdAccount = new Account(1L, 1L, "USD", new BigDecimal("0"), 1L);

    private List<Account> accounts = Lists.newArrayList(
            usdAccount,
            new Account(2L, 2L, "EUR", new BigDecimal("10.50"), 1L),
            new Account(3L, 3L, "GBP", new BigDecimal("5.50"), 1L)
    );

    private CustomerDto customerDto = new CustomerDto.CustomerDtoBuilder()
            .id(8L)
            .dateOfBirth(LocalDate.parse("1978-08-23"))
            .firstName("Kobe")
            .lastName("Bryant")
            .build();

    @Before
    public void setup() {
        when(accountDao.findAll()).thenReturn(accounts);
        when(accountDao.findById(1L)).thenReturn(usdAccount);
        when(accountDao.findById(9999L)).thenReturn(null);
        when(accountDao.getId(any())).thenReturn(24L);
        when(accountDao.findById(24L)).thenReturn(createdAccount);
        when(customerService.getById(8L)).thenReturn(customerDto);
        when(bankService.isCurrencySupported("MXN")).thenReturn(false);
        when(bankService.isCurrencySupported("USD")).thenReturn(true);
        accountService = new AccountService(new AccountConverter(), accountDao, customerService, bankService);
    }

    @Test
    public void test_FindAll() {
        List<AccountDto> accountDtos = accountService.findAll();
        assertEquals(3, accountDtos.size());
    }

    @Test
    public void test_getById() {
        AccountDto accountDto = accountService.getById(1L);
        assertEquals(usdAccount.getId(), accountDto.getId());
        assertEquals(usdAccount.getCustomerId(), accountDto.getCustomerId());
        assertEquals(usdAccount.getBalance(), accountDto.getBalance());
        assertEquals(usdAccount.getCurrency(), accountDto.getCurrency());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void test_getById_Null() {
        accountService.getById(9999L);
    }

    @Test
    public void test_Create() {
        AccountCreateDto createDto = new AccountCreateDto(8L, "USD");
        AccountDto account = accountService.create(createDto);
        assertEquals(Long.valueOf(1), account.getId());
        assertEquals(new BigDecimal("0"), account.getBalance());
        assertEquals(Long.valueOf(1), account.getCustomerId());
        assertEquals("USD", account.getCurrency());
    }

    @Test(expected = CurrencyNotSupportedException.class)
    public void test_Create_CurrencyNotSupported() {
        AccountCreateDto createDto = new AccountCreateDto(8L, "MXN");
        accountService.create(createDto);
    }
}
