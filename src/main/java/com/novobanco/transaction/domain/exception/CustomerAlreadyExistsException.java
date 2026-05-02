package com.novobanco.transaction.domain.exception;

public class CustomerAlreadyExistsException extends DomainException {

    public CustomerAlreadyExistsException(String field, String value) {
        super("Ya existe un cliente con " + field + ": " + value);
    }
}
