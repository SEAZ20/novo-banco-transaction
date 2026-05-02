package com.novobanco.transaction.application.service;

import com.novobanco.transaction.application.port.input.command.WithdrawCommand;
import com.novobanco.transaction.application.port.output.AccountOutputPort;
import com.novobanco.transaction.application.port.output.TransactionOutputPort;
import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.exception.DuplicateTransactionException;
import com.novobanco.transaction.domain.exception.InsufficientFundsException;
import com.novobanco.transaction.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WithdrawServiceTest {

    @Mock AccountOutputPort accountOutputPort;
    @Mock TransactionOutputPort transactionOutputPort;

    @InjectMocks
    WithdrawService service;

    private static final String ACCOUNT_NUMBER = "NB0000000001";
    private static final String REFERENCE = "REF-WD-001";

    private Account accountWithBalance(String amount) {
        Account acc = new Account(1L, ACCOUNT_NUMBER, 10L, AccountType.SAVINGS, "USD",
                Money.ZERO, AccountStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
        acc.deposit(Money.of(amount));
        return acc;
    }

    private WithdrawCommand command(String amount) {
        return new WithdrawCommand(ACCOUNT_NUMBER, new BigDecimal(amount), "Retiro", REFERENCE);
    }

    @Test
    void withdraw_success_returnsTransaction() {
        Account account = accountWithBalance("1000.00");
        given(transactionOutputPort.existsByReference(REFERENCE)).willReturn(false);
        given(accountOutputPort.findByAccountNumberForUpdate(ACCOUNT_NUMBER))
                .willReturn(Optional.of(account));
        given(accountOutputPort.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(transactionOutputPort.save(any())).willAnswer(inv -> inv.getArgument(0));

        Transaction result = service.withdraw(command("300.00"));

        assertThat(result.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(account.getBalance().value()).isEqualByComparingTo("700.00");
        verify(accountOutputPort).save(account);
    }

    @Test
    void withdraw_duplicateReference_throwsDuplicateTransactionException() {
        given(transactionOutputPort.existsByReference(REFERENCE)).willReturn(true);

        assertThatExceptionOfType(DuplicateTransactionException.class)
                .isThrownBy(() -> service.withdraw(command("100.00")));

        verify(accountOutputPort, never()).findByAccountNumberForUpdate(any());
    }

    @Test
    void withdraw_accountNotFound_throwsAccountNotFoundException() {
        given(transactionOutputPort.existsByReference(REFERENCE)).willReturn(false);
        given(accountOutputPort.findByAccountNumberForUpdate(ACCOUNT_NUMBER))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() -> service.withdraw(command("100.00")));
    }

    @Test
    void withdraw_insufficientFunds_throwsInsufficientFundsException() {
        Account account = accountWithBalance("50.00");
        given(transactionOutputPort.existsByReference(REFERENCE)).willReturn(false);
        given(accountOutputPort.findByAccountNumberForUpdate(ACCOUNT_NUMBER))
                .willReturn(Optional.of(account));

        assertThatExceptionOfType(InsufficientFundsException.class)
                .isThrownBy(() -> service.withdraw(command("200.00")));

        verify(accountOutputPort, never()).save(any());
    }
}
