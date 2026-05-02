package com.novobanco.transaction.domain.model;

import com.novobanco.transaction.domain.exception.InactiveAccountException;
import com.novobanco.transaction.domain.exception.InsufficientFundsException;
import com.novobanco.transaction.domain.exception.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.create("NB0000000001", 1L, AccountType.SAVINGS);
    }

    // =========================================================================
    // create()
    // =========================================================================

    @Test
    void create_initializedWithZeroBalanceAndActiveStatus() {
        assertThat(account.getBalance()).isEqualTo(Money.ZERO);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getCurrency()).isEqualTo("USD");
        assertThat(account.getType()).isEqualTo(AccountType.SAVINGS);
    }

    // =========================================================================
    // deposit()
    // =========================================================================

    @Test
    void deposit_increasesBalanceByAmount() {
        account.deposit(Money.of("500.00"));
        assertThat(account.getBalance().value()).isEqualByComparingTo("500.00");
    }

    @Test
    void deposit_accumulatesOnMultipleCalls() {
        account.deposit(Money.of("300.00"));
        account.deposit(Money.of("200.00"));
        assertThat(account.getBalance().value()).isEqualByComparingTo("500.00");
    }

    @Test
    void deposit_zeroAmount_throwsInvalidAmountException() {
        assertThatExceptionOfType(InvalidAmountException.class)
                .isThrownBy(() -> account.deposit(Money.ZERO));
    }

    @Test
    void deposit_negativeAmount_throwsInvalidAmountException() {
        assertThatExceptionOfType(InvalidAmountException.class)
                .isThrownBy(() -> account.deposit(Money.of("-1")));
    }

    @Test
    void deposit_onBlockedAccount_throwsInactiveAccountException() {
        account.block();
        assertThatExceptionOfType(InactiveAccountException.class)
                .isThrownBy(() -> account.deposit(Money.of("100")));
    }

    @Test
    void deposit_onClosedAccount_throwsInactiveAccountException() {
        account.close();
        assertThatExceptionOfType(InactiveAccountException.class)
                .isThrownBy(() -> account.deposit(Money.of("100")));
    }

    // =========================================================================
    // withdraw()
    // =========================================================================

    @Test
    void withdraw_decreasesBalanceByAmount() {
        account.deposit(Money.of("1000.00"));
        account.withdraw(Money.of("400.00"));
        assertThat(account.getBalance().value()).isEqualByComparingTo("600.00");
    }

    @Test
    void withdraw_exactBalance_succeeds() {
        account.deposit(Money.of("100.00"));
        account.withdraw(Money.of("100.00"));
        assertThat(account.getBalance().value()).isEqualByComparingTo("0.00");
    }

    @Test
    void withdraw_insufficientFunds_throwsInsufficientFundsException() {
        account.deposit(Money.of("50.00"));
        assertThatExceptionOfType(InsufficientFundsException.class)
                .isThrownBy(() -> account.withdraw(Money.of("100.00")));
    }

    @Test
    void withdraw_zeroAmount_throwsInvalidAmountException() {
        assertThatExceptionOfType(InvalidAmountException.class)
                .isThrownBy(() -> account.withdraw(Money.ZERO));
    }

    @Test
    void withdraw_onBlockedAccount_throwsInactiveAccountException() {
        account.block();
        assertThatExceptionOfType(InactiveAccountException.class)
                .isThrownBy(() -> account.withdraw(Money.of("100")));
    }

    @Test
    void withdraw_onClosedAccount_throwsInactiveAccountException() {
        account.close();
        assertThatExceptionOfType(InactiveAccountException.class)
                .isThrownBy(() -> account.withdraw(Money.of("100")));
    }

    // =========================================================================
    // block() / close()
    // =========================================================================

    @Test
    void block_changesStatusToBlocked() {
        account.block();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.BLOCKED);
    }

    @Test
    void close_changesStatusToClosed() {
        account.close();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    void block_onClosedAccount_throwsInactiveAccountException() {
        account.close();
        assertThatExceptionOfType(InactiveAccountException.class)
                .isThrownBy(() -> account.block());
    }
}
