package com.novobanco.transaction.domain.port.input;

import com.novobanco.transaction.domain.model.Account;
import com.novobanco.transaction.domain.port.input.command.CreateAccountCommand;

public interface CreateAccountUseCase {
    Account createAccount(CreateAccountCommand command);
}
