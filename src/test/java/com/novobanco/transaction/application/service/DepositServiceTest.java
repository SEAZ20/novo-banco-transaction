package com.novobanco.transaction.application.service;

import com.novobanco.transaction.application.port.input.command.DepositCommand;
import com.novobanco.transaction.application.port.output.AccountOutputPort;
import com.novobanco.transaction.application.port.output.TransactionOutputPort;
import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.exception.DuplicateTransactionException;
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
class DepositServiceTest {

    @Mock AccountOutputPort accountOutputPort;
    @Mock TransactionOutputPort transactionOutputPort;

    @InjectMocks
    DepositService service;

    private static final String ACCOUNT_NUMBER = "NB0000000001";
    private static final String REFERENCE = "REF-001";
    private static final DepositCommand COMMAND =
            new DepositCommand(ACCOUNT_NUMBER, new BigDecimal("500.00"), "Depósito", REFERENCE);

    private Account activeAccount() {
        return new Account(1L, ACCOUNT_NUMBER, 10L, AccountType.SAVINGS, "USD",
                Money.ZERO, AccountStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
    }

    private Transaction savedTransaction() {
        return Transaction.ofDeposit(REFERENCE, 1L, Money.of("500.00"), Money.of("500.00"), "Depósito");
    }

    @Test
    void deposit_success_returnsTransaction() {
        given(transactionOutputPort.existsByReference(REFERENCE)).willReturn(false);
        given(accountOutputPort.findByAccountNumberForUpdate(ACCOUNT_NUMBER))
                .willReturn(Optional.of(activeAccount()));
        given(accountOutputPort.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));
        given(transactionOutputPort.save(any(Transaction.class))).willReturn(savedTransaction());

        Transaction result = service.deposit(COMMAND);

        assertThat(result.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        verify(accountOutputPort).save(any(Account.class));
        verify(transactionOutputPort).save(any(Transaction.class));
    }

    @Test
    void deposit_balanceUpdatedCorrectly() {
        Account account = activeAccount();
        given(transactionOutputPort.existsByReference(REFERENCE)).willReturn(false);
        given(accountOutputPort.findByAccountNumberForUpdate(ACCOUNT_NUMBER))
                .willReturn(Optional.of(account));
        given(accountOutputPort.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(transactionOutputPort.save(any())).willReturn(savedTransaction());

        service.deposit(COMMAND);

        assertThat(account.getBalance().value()).isEqualByComparingTo("500.00");
    }

    @Test
    void deposit_duplicateReference_throwsDuplicateTransactionException() {
        given(transactionOutputPort.existsByReference(REFERENCE)).willReturn(true);

        assertThatExceptionOfType(DuplicateTransactionException.class)
                .isThrownBy(() -> service.deposit(COMMAND));

        verify(accountOutputPort, never()).findByAccountNumberForUpdate(any());
    }

    @Test
    void deposit_accountNotFound_throwsAccountNotFoundException() {
        given(transactionOutputPort.existsByReference(REFERENCE)).willReturn(false);
        given(accountOutputPort.findByAccountNumberForUpdate(ACCOUNT_NUMBER))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() -> service.deposit(COMMAND));
    }
}
