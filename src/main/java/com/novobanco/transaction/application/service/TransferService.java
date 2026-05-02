package com.novobanco.transaction.application.service;

import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.exception.DuplicateTransactionException;
import com.novobanco.transaction.domain.model.Account;
import com.novobanco.transaction.domain.model.Money;
import com.novobanco.transaction.domain.model.Transaction;
import com.novobanco.transaction.application.port.input.TransferUseCase;
import com.novobanco.transaction.application.port.input.command.TransferCommand;
import com.novobanco.transaction.application.port.input.command.TransferResult;
import com.novobanco.transaction.application.port.output.AccountOutputPort;
import com.novobanco.transaction.application.port.output.TransactionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService implements TransferUseCase {

    private final AccountOutputPort accountOutputPort;
    private final TransactionOutputPort transactionOutputPort;

    @Override
    @Transactional
    public TransferResult transfer(TransferCommand command) {
        if (transactionOutputPort.existsByReference(command.reference())) {
            throw new DuplicateTransactionException(command.reference());
        }

        // -----------------------------------------------------------------------
        // Anti-deadlock: siempre se bloquean las cuentas en orden alfabético.
        // Si dos transferencias A→B y B→A ocurren simultáneamente, ambas esperan
        // el mismo lock primero → no hay ciclo → no hay deadlock.
        // -----------------------------------------------------------------------
        List<String> ordered = List.of(command.sourceAccountNumber(), command.targetAccountNumber())
                .stream().sorted().toList();

        Account first  = lockAccount(ordered.get(0));
        Account second = lockAccount(ordered.get(1));

        Account source = first.getAccountNumber().equals(command.sourceAccountNumber()) ? first : second;
        Account target = first.getAccountNumber().equals(command.targetAccountNumber()) ? first : second;

        Money amount = Money.of(command.amount());

        // -----------------------------------------------------------------------
        // Atomicidad: ambas operaciones ocurren dentro de la misma transacción
        // @Transactional. Si el crédito falla por cualquier razón, el débito
        // se revierte automáticamente (rollback de Oracle). No queda estado parcial.
        // -----------------------------------------------------------------------
        source.withdraw(amount);   // valida: ACTIVE + fondos suficientes
        target.deposit(amount);    // valida: ACTIVE + monto > 0

        accountOutputPort.save(source);
        accountOutputPort.save(target);

        Transaction debit = Transaction.ofTransferDebit(
                command.reference(), source.getId(), target.getId(),
                amount, source.getBalance(), command.description()
        );
        // La pata de crédito lleva su propia referencia única (no enviada por el cliente)
        Transaction credit = Transaction.ofTransferCredit(
                UUID.randomUUID().toString(), target.getId(), source.getId(),
                amount, target.getBalance(), command.description()
        );

        return new TransferResult(
                transactionOutputPort.save(debit),
                transactionOutputPort.save(credit)
        );
    }

    private Account lockAccount(String accountNumber) {
        return accountOutputPort.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }
}
