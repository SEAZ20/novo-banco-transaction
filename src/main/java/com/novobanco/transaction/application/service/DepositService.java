package com.novobanco.transaction.application.service;

import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.exception.DomainException;
import com.novobanco.transaction.domain.exception.DuplicateTransactionException;
import com.novobanco.transaction.domain.model.Account;
import com.novobanco.transaction.domain.model.Money;
import com.novobanco.transaction.domain.model.Transaction;
import com.novobanco.transaction.domain.model.TransactionStatus;
import com.novobanco.transaction.application.port.input.DepositUseCase;
import com.novobanco.transaction.application.port.input.command.DepositCommand;
import com.novobanco.transaction.application.port.output.AccountOutputPort;
import com.novobanco.transaction.application.port.output.TransactionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepositService implements DepositUseCase {

    private final AccountOutputPort accountOutputPort;
    private final TransactionOutputPort transactionOutputPort;
    private final FailedTransactionRecorder failedTransactionRecorder;

    @Override
    @Transactional
    public Transaction deposit(DepositCommand command) {
        Optional<Transaction> existing = transactionOutputPort.findByReference(command.reference());
        if (existing.isPresent()) {
            if (existing.get().getStatus() == TransactionStatus.SUCCESS) return existing.get();
            throw new DuplicateTransactionException(command.reference());
        }

        Account account = accountOutputPort.findByAccountNumberForUpdate(command.accountNumber())
                .orElseThrow(() -> new AccountNotFoundException(command.accountNumber()));

        Money amount = Money.of(command.amount());
        Money balanceBefore = account.getBalance();

        try {
            account.deposit(amount);
        } catch (DomainException e) {
            failedTransactionRecorder.record(
                    Transaction.ofFailedDeposit(command.reference(), account.getId(),
                            amount, balanceBefore, command.description())
            );
            throw e;
        }

        accountOutputPort.save(account);
        return transactionOutputPort.save(
                Transaction.ofDeposit(command.reference(), account.getId(),
                        amount, account.getBalance(), command.description())
        );
    }
}
