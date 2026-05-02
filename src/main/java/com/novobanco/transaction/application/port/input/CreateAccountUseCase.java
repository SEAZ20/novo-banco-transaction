package com.novobanco.transaction.application.port.input;

import com.novobanco.transaction.domain.model.Account;
import com.novobanco.transaction.application.port.input.command.CreateAccountCommand;

public interface CreateAccountUseCase {
    Account createAccount(CreateAccountCommand command);
}
