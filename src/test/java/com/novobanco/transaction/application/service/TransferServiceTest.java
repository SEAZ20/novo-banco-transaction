package com.novobanco.transaction.application.service;

import com.novobanco.transaction.application.port.input.command.TransferCommand;
import com.novobanco.transaction.application.port.input.command.TransferResult;
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
class TransferServiceTest {

    @Mock AccountOutputPort accountOutputPort;
    @Mock TransactionOutputPort transactionOutputPort;

    @InjectMocks
    TransferService service;

    private static final String SOURCE = "NB0000000001";
    private static final String TARGET = "NB0000000002";
    private static final String REFERENCE = "REF-TR-001";

    private TransferCommand command(String amount) {
        return new TransferCommand(SOURCE, TARGET, new BigDecimal(amount), "Transferencia", REFERENCE);
    }

    private Account accountWithBalance(Long id, String number, String balance) {
        Account acc = new Account(id, number, 10L, AccountType.SAVINGS, "USD",
                Money.ZERO, AccountStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
        if (new BigDecimal(balance).compareTo(BigDecimal.ZERO) > 0) {
            acc.deposit(Money.of(balance));
        }
        return acc;
    }

    @Test
    void transfer_success_returnsBothTransactions() {
        Account source = accountWithBalance(1L, SOURCE, "1000.00");
        Account target = accountWithBalance(2L, TARGET, "200.00");

        given(transactionOutputPort.existsByReference(REFERENCE)).willReturn(false);
        // El servicio bloquea en orden alfabético: SOURCE < TARGET
        given(accountOutputPort.findByAccountNumberForUpdate(SOURCE)).willReturn(Optional.of(source));
        given(accountOutputPort.findByAccountNumberForUpdate(TARGET)).willReturn(Optional.of(target));
        given(accountOutputPort.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(transactionOutputPort.save(any())).willAnswer(inv -> inv.getArgument(0));

        TransferResult result = service.transfer(command("300.00"));

        assertThat(result.debit().getType()).isEqualTo(TransactionType.TRANSFER_DEBIT);
        assertThat(result.credit().getType()).isEqualTo(TransactionType.TRANSFER_CREDIT);
        assertThat(source.getBalance().value()).isEqualByComparingTo("700.00");
        assertThat(target.getBalance().value()).isEqualByComparingTo("500.00");
    }

    @Test
    void transfer_duplicateReference_throwsDuplicateTransactionException() {
        given(transactionOutputPort.existsByReference(REFERENCE)).willReturn(true);

        assertThatExceptionOfType(DuplicateTransactionException.class)
                .isThrownBy(() -> service.transfer(command("100.00")));

        verify(accountOutputPort, never()).findByAccountNumberForUpdate(any());
    }

    @Test
    void transfer_sourceAccountNotFound_throwsAccountNotFoundException() {
        given(transactionOutputPort.existsByReference(REFERENCE)).willReturn(false);
        // El servicio bloquea en orden alfabético: SOURCE primero → falla aquí → TARGET nunca se consulta
        given(accountOutputPort.findByAccountNumberForUpdate(SOURCE)).willReturn(Optional.empty());

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() -> service.transfer(command("100.00")));
    }

    @Test
    void transfer_insufficientFunds_throwsInsufficientFundsException() {
        Account source = accountWithBalance(1L, SOURCE, "50.00");
        Account target = accountWithBalance(2L, TARGET, "0");

        given(transactionOutputPort.existsByReference(REFERENCE)).willReturn(false);
        given(accountOutputPort.findByAccountNumberForUpdate(SOURCE)).willReturn(Optional.of(source));
        given(accountOutputPort.findByAccountNumberForUpdate(TARGET)).willReturn(Optional.of(target));

        assertThatExceptionOfType(InsufficientFundsException.class)
                .isThrownBy(() -> service.transfer(command("500.00")));

        verify(accountOutputPort, never()).save(any());
    }
}
