package com.novobanco.transaction.domain.model;

import com.novobanco.transaction.domain.exception.InactiveAccountException;
import com.novobanco.transaction.domain.exception.InsufficientFundsException;
import com.novobanco.transaction.domain.exception.InvalidAmountException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
public class Account {

    private Long id;
    private final String accountNumber;
    private final Long customerId;
    private final AccountType type;
    private final String currency;
    private Money balance;
    private AccountStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static Account create(String accountNumber, Long customerId, AccountType type) {
        LocalDateTime now = LocalDateTime.now();
        return new Account(null, accountNumber, customerId, type, "USD",
                Money.ZERO, AccountStatus.ACTIVE, now, now);
    }

    // =========================================================================
    // Reglas de negocio
    // =========================================================================

    public void deposit(Money amount) {
        validateIsActive();
        validatePositiveAmount(amount);
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void withdraw(Money amount) {
        validateIsActive();
        validatePositiveAmount(amount);
        validateSufficientFunds(amount);
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void block() {
        if (this.status == AccountStatus.CLOSED) {
            throw new InactiveAccountException(accountNumber, status);
        }
        this.status = AccountStatus.BLOCKED;
        this.updatedAt = LocalDateTime.now();
    }

    public void close() {
        this.status = AccountStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    public void assignId(Long id) {
        if (this.id != null) throw new IllegalStateException("El id de la cuenta ya fue asignado");
        this.id = id;
    }

    // =========================================================================
    // Validaciones privadas
    // =========================================================================

    private void validateIsActive() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new InactiveAccountException(accountNumber, status);
        }
    }

    private void validatePositiveAmount(Money amount) {
        if (!amount.isPositive()) {
            throw new InvalidAmountException(amount.value());
        }
    }

    private void validateSufficientFunds(Money amount) {
        if (!balance.isGreaterThanOrEqualTo(amount)) {
            throw new InsufficientFundsException(accountNumber, amount, balance);
        }
    }
}
