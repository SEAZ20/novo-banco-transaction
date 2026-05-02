package com.novobanco.transaction.application.port.input;

import com.novobanco.transaction.domain.model.Transaction;
import com.novobanco.transaction.application.port.input.command.WithdrawCommand;

public interface WithdrawUseCase {
    Transaction withdraw(WithdrawCommand command);
}
