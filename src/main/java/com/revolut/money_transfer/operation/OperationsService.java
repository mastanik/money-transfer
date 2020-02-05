package com.revolut.money_transfer.operation;

import com.revolut.money_transfer.operation.dto.DepositDto;
import com.revolut.money_transfer.operation.dto.TransferDto;
import com.revolut.money_transfer.operation.dto.WithdrawDto;

public interface OperationsService {
    void deposit(final DepositDto depositDto);

    void withdraw(final WithdrawDto withdrawDto);

    void transfer(final TransferDto transferDto);
}
