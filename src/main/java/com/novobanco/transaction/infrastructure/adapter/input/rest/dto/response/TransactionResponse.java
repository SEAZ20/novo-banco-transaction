package com.novobanco.transaction.infrastructure.adapter.input.rest.dto.response;

import com.novobanco.transaction.domain.model.Transaction;
import com.novobanco.transaction.domain.model.TransactionStatus;
import com.novobanco.transaction.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String reference,
        Long accountId,
        Long relatedAccountId,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        TransactionStatus status,
        String description,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getReference(),
                tx.getAccountId(),
                tx.getRelatedAccountId(),
                tx.getType(),
                tx.getAmount().value(),
                tx.getBalanceAfter().value(),
                tx.getStatus(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }
}
