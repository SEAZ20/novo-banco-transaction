package com.novobanco.transaction.domain.exception;

public class AccountNotFoundException extends DomainException {

    public AccountNotFoundException(String accountNumber) {
        super("Cuenta no encontrada: " + accountNumber);
    }
}
