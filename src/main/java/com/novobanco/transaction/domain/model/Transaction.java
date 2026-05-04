package com.novobanco.transaction.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Registro inmutable de un movimiento financiero.
 * Una vez creada, solo puede cambiar su status (reverse/failed).
 */
@Getter
public class Transaction {

    private Long id;
    private final String reference;
    private final Long accountId;
    private final Long relatedAccountId;
    private final TransactionType type;
    private final Money amount;
    private final Money balanceAfter;
    private TransactionStatus status;
    private final String description;
    private final LocalDateTime createdAt;

    public Transaction(Long id, String reference, Long accountId, Long relatedAccountId,
                       TransactionType type, Money amount, Money balanceAfter,
                       TransactionStatus status, String description, LocalDateTime createdAt) {
        this.id = id;
        this.reference = reference;
        this.accountId = accountId;
        this.relatedAccountId = relatedAccountId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
    }

    // =========================================================================
    // Factory methods por tipo de operación
    // =========================================================================

    public static Transaction ofDeposit(String reference, Long accountId,
                                        Money amount, Money balanceAfter, String description) {
        return new Transaction(null, reference, accountId, null,
                TransactionType.DEPOSIT, amount, balanceAfter,
                TransactionStatus.SUCCESS, description, LocalDateTime.now());
    }

    public static Transaction ofWithdrawal(String reference, Long accountId,
                                           Money amount, Money balanceAfter, String description) {
        return new Transaction(null, reference, accountId, null,
                TransactionType.WITHDRAWAL, amount, balanceAfter,
                TransactionStatus.SUCCESS, description, LocalDateTime.now());
    }

    public static Transaction ofTransferDebit(String reference, Long accountId, Long relatedAccountId,
                                              Money amount, Money balanceAfter, String description) {
        return new Transaction(null, reference, accountId, relatedAccountId,
                TransactionType.TRANSFER_DEBIT, amount, balanceAfter,
                TransactionStatus.SUCCESS, description, LocalDateTime.now());
    }

    public static Transaction ofTransferCredit(String reference, Long accountId, Long relatedAccountId,
                                               Money amount, Money balanceAfter, String description) {
        return new Transaction(null, reference, accountId, relatedAccountId,
                TransactionType.TRANSFER_CREDIT, amount, balanceAfter,
                TransactionStatus.SUCCESS, description, LocalDateTime.now());
    }

    // =========================================================================
    // Factory methods para transacciones fallidas (FAILED)
    // =========================================================================

    public static Transaction ofFailedDeposit(String reference, Long accountId,
                                              Money amount, Money balanceBefore, String description) {
        return new Transaction(null, reference, accountId, null,
                TransactionType.DEPOSIT, amount, balanceBefore,
                TransactionStatus.FAILED, description, LocalDateTime.now());
    }

    public static Transaction ofFailedWithdrawal(String reference, Long accountId,
                                                 Money amount, Money balanceBefore, String description) {
        return new Transaction(null, reference, accountId, null,
                TransactionType.WITHDRAWAL, amount, balanceBefore,
                TransactionStatus.FAILED, description, LocalDateTime.now());
    }

    public static Transaction ofFailedTransferDebit(String reference, Long accountId, Long relatedAccountId,
                                                    Money amount, Money balanceBefore, String description) {
        return new Transaction(null, reference, accountId, relatedAccountId,
                TransactionType.TRANSFER_DEBIT, amount, balanceBefore,
                TransactionStatus.FAILED, description, LocalDateTime.now());
    }

    // =========================================================================
    // Cambios de estado permitidos
    // =========================================================================

    public void reverse() {
        this.status = TransactionStatus.REVERSED;
    }

    public void markFailed() {
        this.status = TransactionStatus.FAILED;
    }

    public void assignId(Long id) {
        if (this.id != null) throw new IllegalStateException("El id de la transacción ya fue asignado");
        this.id = id;
    }
}
