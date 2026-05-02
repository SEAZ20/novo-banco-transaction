package com.novobanco.transaction.domain.port.input.command;

import java.math.BigDecimal;

public record DepositCommand(
        String accountNumber,
        BigDecimal amount,
        String description,
        String reference
) {}
