package com.novobanco.transaction.infrastructure.adapter.input.rest.dto.response;

import com.novobanco.transaction.domain.model.Account;
import com.novobanco.transaction.domain.model.AccountStatus;
import com.novobanco.transaction.domain.model.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String accountNumber,
        Long customerId,
        AccountType type,
        String currency,
        BigDecimal balance,
        AccountStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                account.getType(),
                account.getCurrency(),
                account.getBalance().value(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
