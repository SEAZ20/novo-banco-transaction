package com.novobanco.transaction.domain.exception;

public class CustomerNotFoundException extends DomainException {

    public CustomerNotFoundException(Long customerId) {
        super("Cliente no encontrado con id: " + customerId);
    }
}
