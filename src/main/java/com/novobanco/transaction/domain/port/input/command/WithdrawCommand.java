package com.novobanco.transaction.domain.port.input.command;

import java.math.BigDecimal;

public record WithdrawCommand(
        String accountNumber,
        BigDecimal amount,
        String description,
        String reference
) {}
