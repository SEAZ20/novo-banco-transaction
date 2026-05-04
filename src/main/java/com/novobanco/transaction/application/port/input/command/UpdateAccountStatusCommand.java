package com.novobanco.transaction.application.port.input.command;

import com.novobanco.transaction.domain.model.AccountStatus;

public record UpdateAccountStatusCommand(String accountNumber, AccountStatus status) {}
