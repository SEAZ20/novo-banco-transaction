package com.novobanco.transaction.application.port.input;

import com.novobanco.transaction.application.port.input.command.UpdateAccountStatusCommand;
import com.novobanco.transaction.domain.model.Account;

public interface UpdateAccountStatusUseCase {
    Account updateStatus(UpdateAccountStatusCommand command);
}
