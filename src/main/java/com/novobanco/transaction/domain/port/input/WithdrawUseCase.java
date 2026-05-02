package com.novobanco.transaction.domain.port.input;

import com.novobanco.transaction.domain.model.Transaction;
import com.novobanco.transaction.domain.port.input.command.WithdrawCommand;

public interface WithdrawUseCase {
    Transaction withdraw(WithdrawCommand command);
}
