package com.novobanco.transaction.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class MoneyTest {

    @Test
    void of_setsScale4WithHalfUpRounding() {
        Money money = Money.of("10.12345");
        assertThat(money.value()).isEqualByComparingTo("10.1235");
    }

    @Test
    void add_returnsSumWithScale4() {
        Money result = Money.of("100.00").add(Money.of("50.5000"));
        assertThat(result.value()).isEqualByComparingTo("150.50");
    }

    @Test
    void subtract_returnsDifferenceWithScale4() {
        Money result = Money.of("200.00").subtract(Money.of("75.25"));
        assertThat(result.value()).isEqualByComparingTo("124.75");
    }

    @Test
    void isPositive_trueForPositiveAmount() {
        assertThat(Money.of("0.01").isPositive()).isTrue();
    }

    @Test
    void isPositive_falseForZero() {
        assertThat(Money.ZERO.isPositive()).isFalse();
    }

    @Test
    void isPositive_falseForNegativeAmount() {
        assertThat(Money.of("-1").isPositive()).isFalse();
    }

    @Test
    void isNegative_trueForNegativeAmount() {
        assertThat(Money.of("-0.01").isNegative()).isTrue();
    }

    @Test
    void isNegative_falseForZero() {
        assertThat(Money.ZERO.isNegative()).isFalse();
    }

    @Test
    void isGreaterThanOrEqualTo_equalAmountsReturnsTrue() {
        assertThat(Money.of("100").isGreaterThanOrEqualTo(Money.of("100"))).isTrue();
    }

    @Test
    void isGreaterThanOrEqualTo_greaterAmountReturnsTrue() {
        assertThat(Money.of("100.01").isGreaterThanOrEqualTo(Money.of("100"))).isTrue();
    }

    @Test
    void isGreaterThanOrEqualTo_lesserAmountReturnsFalse() {
        assertThat(Money.of("99.99").isGreaterThanOrEqualTo(Money.of("100"))).isFalse();
    }

    @Test
    void zero_isNotPositiveAndNotNegative() {
        assertThat(Money.ZERO.isPositive()).isFalse();
        assertThat(Money.ZERO.isNegative()).isFalse();
    }

    @Test
    void nullValue_throwsNullPointerException() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Money(null))
                .withMessage("El monto es requerido");
    }

    @Test
    void ofBigDecimal_and_ofString_produceSameResult() {
        Money fromBD = Money.of(new BigDecimal("123.45"));
        Money fromStr = Money.of("123.45");
        assertThat(fromBD).isEqualTo(fromStr);
    }
}
