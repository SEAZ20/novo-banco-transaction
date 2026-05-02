package com.novobanco.transaction.domain.exception;

import java.math.BigDecimal;

public class InvalidAmountException extends DomainException {

    public InvalidAmountException(BigDecimal amount) {
        super("El monto debe ser mayor a cero. Valor recibido: " + amount.toPlainString());
    }
}
