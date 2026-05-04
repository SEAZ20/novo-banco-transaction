package com.novobanco.transaction.application.service;

import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.exception.DomainException;
import com.novobanco.transaction.domain.exception.DuplicateTransactionException;
import com.novobanco.transaction.domain.model.Account;
import com.novobanco.transaction.domain.model.Money;
import com.novobanco.transaction.domain.model.Transaction;
import com.novobanco.transaction.domain.model.TransactionStatus;
import com.novobanco.transaction.application.port.input.TransferUseCase;
import com.novobanco.transaction.application.port.input.command.TransferCommand;
import com.novobanco.transaction.application.port.input.command.TransferResult;
import com.novobanco.transaction.application.port.output.AccountOutputPort;
import com.novobanco.transaction.application.port.output.TransactionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferService implements TransferUseCase {

    private final AccountOutputPort accountOutputPort;
    private final TransactionOutputPort transactionOutputPort;
    private final FailedTransactionRecorder failedTransactionRecorder;

    @Override
    @Transactional
    public TransferResult transfer(TransferCommand command) {
        Optional<Transaction> existingDebit = transactionOutputPort.findByReference(command.reference());
        if (existingDebit.isPresent()) {
            Transaction debit = existingDebit.get();
            if (debit.getStatus() == TransactionStatus.SUCCESS) {
                Transaction credit = transactionOutputPort.findByReference(command.reference() + "-CREDIT")
                        .orElseThrow(() -> new IllegalStateException(
                                "Crédito no encontrado para referencia: " + command.reference()));
                return new TransferResult(debit, credit);
            }
            throw new DuplicateTransactionException(command.reference());
        }

        // Anti-deadlock: bloquear siempre en orden alfabético para evitar ciclos
        List<String> ordered = List.of(command.sourceAccountNumber(), command.targetAccountNumber())
                .stream().sorted().toList();

        Account first  = lockAccount(ordered.get(0));
        Account second = lockAccount(ordered.get(1));

        Account source = first.getAccountNumber().equals(command.sourceAccountNumber()) ? first : second;
        Account target = first.getAccountNumber().equals(command.targetAccountNumber()) ? first : second;

        Money amount = Money.of(command.amount());
        Money sourceBalanceBefore = source.getBalance();

        try {
            source.withdraw(amount);   // valida: ACTIVE + fondos suficientes
            target.deposit(amount);    // valida: ACTIVE + monto > 0
        } catch (DomainException e) {
            failedTransactionRecorder.record(
                    Transaction.ofFailedTransferDebit(command.reference(), source.getId(), target.getId(),
                            amount, sourceBalanceBefore, command.description())
            );
            throw e;
        }

        accountOutputPort.save(source);
        accountOutputPort.save(target);

        Transaction debit = Transaction.ofTransferDebit(
                command.reference(), source.getId(), target.getId(),
                amount, source.getBalance(), command.description()
        );
        Transaction credit = Transaction.ofTransferCredit(
                command.reference() + "-CREDIT", target.getId(), source.getId(),
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
