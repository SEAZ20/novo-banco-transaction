package com.novobanco.transaction.domain.exception;

public class DuplicateTransactionException extends DomainException {

    public DuplicateTransactionException(String reference) {
        super("Ya existe una transacción con la referencia: " + reference
              + ". La operación fue rechazada para evitar un duplicado.");
    }
}
