package com.novobanco.transaction.application.port.input.command;

import java.math.BigDecimal;

public record TransferCommand(
        String sourceAccountNumber,
        String targetAccountNumber,
        BigDecimal amount,
        String description,
        String reference
) {}
