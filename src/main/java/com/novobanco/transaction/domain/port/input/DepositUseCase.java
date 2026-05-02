package com.novobanco.transaction.domain.port.input;

import com.novobanco.transaction.domain.model.Transaction;
import com.novobanco.transaction.domain.port.input.command.DepositCommand;

public interface DepositUseCase {
    Transaction deposit(DepositCommand command);
}
