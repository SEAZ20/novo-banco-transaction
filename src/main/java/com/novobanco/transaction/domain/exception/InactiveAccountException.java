package com.novobanco.transaction.domain.exception;

import com.novobanco.transaction.domain.model.AccountStatus;

public class InactiveAccountException extends DomainException {

    public InactiveAccountException(String accountNumber, AccountStatus status) {
        super("La cuenta " + accountNumber + " no puede operar: estado actual es " + status.name()
              + ". Solo las cuentas ACTIVE pueden realizar transacciones.");
    }
}
