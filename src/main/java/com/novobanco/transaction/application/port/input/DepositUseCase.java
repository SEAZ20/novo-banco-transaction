package com.novobanco.transaction.application.port.input;

import com.novobanco.transaction.domain.model.Transaction;
import com.novobanco.transaction.application.port.input.command.DepositCommand;

public interface DepositUseCase {
    Transaction deposit(DepositCommand command);
}
