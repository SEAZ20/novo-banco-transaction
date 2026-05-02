package com.novobanco.transaction.application.port.input.command;

import com.novobanco.transaction.domain.model.AccountType;

public record CreateAccountCommand(Long customerId, AccountType type) {}
