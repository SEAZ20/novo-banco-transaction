package com.novobanco.transaction.application.service;

import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.exception.DuplicateTransactionException;
import com.novobanco.transaction.domain.model.Account;
import com.novobanco.transaction.domain.model.Money;
import com.novobanco.transaction.domain.model.Transaction;
import com.novobanco.transaction.application.port.input.DepositUseCase;
import com.novobanco.transaction.application.port.input.command.DepositCommand;
import com.novobanco.transaction.application.port.output.AccountOutputPort;
import com.novobanco.transaction.application.port.output.TransactionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepositService implements DepositUseCase {

    private final AccountOutputPort accountOutputPort;
    private final TransactionOutputPort transactionOutputPort;

    @Override
    @Transactional
    public Transaction deposit(DepositCommand command) {
        if (transactionOutputPort.existsByReference(command.reference())) {
            throw new DuplicateTransactionException(command.reference());
        }

        // SELECT FOR UPDATE — previene que dos depósitos simultáneos corrompan el saldo
        Account account = accountOutputPort.findByAccountNumberForUpdate(command.accountNumber())
                .orElseThrow(() -> new AccountNotFoundException(command.accountNumber()));

        Money amount = Money.of(command.amount());

        // Valida: cuenta ACTIVE + monto > 0 (reglas en la entidad de dominio)
        account.deposit(amount);
        accountOutputPort.save(account);

        Transaction transaction = Transaction.ofDeposit(
                command.reference(), account.getId(), amount, account.getBalance(), command.description()
        );
        return transactionOutputPort.save(transaction);
    }
}
