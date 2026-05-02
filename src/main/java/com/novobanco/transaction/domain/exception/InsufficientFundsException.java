package com.novobanco.transaction.domain.exception;

import com.novobanco.transaction.domain.model.Money;

public class InsufficientFundsException extends DomainException {

    public InsufficientFundsException(String accountNumber, Money requested, Money available) {
        super("Fondos insuficientes en la cuenta " + accountNumber
              + ". Solicitado: " + requested + " USD, disponible: " + available + " USD.");
    }
}
