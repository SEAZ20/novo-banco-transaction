package com.novobanco.transaction.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa un monto monetario con precisión de 4 decimales.
 * Inmutable. No valida positividad — esa regla vive en las entidades de dominio.
 */
public record Money(BigDecimal value) {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        Objects.requireNonNull(value, "El monto es requerido");
        value = value.setScale(4, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public Money add(Money other) {
        return new Money(value.add(other.value));
    }

    public Money subtract(Money other) {
        return new Money(value.subtract(other.value));
    }

    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return value.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isGreaterThanOrEqualTo(Money other) {
        return value.compareTo(other.value) >= 0;
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}
