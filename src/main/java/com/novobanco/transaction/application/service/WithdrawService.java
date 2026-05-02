package com.novobanco.transaction.application.service;

import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.exception.DuplicateTransactionException;
import com.novobanco.transaction.domain.model.Account;
import com.novobanco.transaction.domain.model.Money;
import com.novobanco.transaction.domain.model.Transaction;
import com.novobanco.transaction.application.port.input.WithdrawUseCase;
import com.novobanco.transaction.application.port.input.command.WithdrawCommand;
import com.novobanco.transaction.application.port.output.AccountOutputPort;
import com.novobanco.transaction.application.port.output.TransactionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawService implements WithdrawUseCase {

    private final AccountOutputPort accountOutputPort;
    private final TransactionOutputPort transactionOutputPort;

    @Override
    @Transactional
    public Transaction withdraw(WithdrawCommand command) {
        if (transactionOutputPort.existsByReference(command.reference())) {
            throw new DuplicateTransactionException(command.reference());
        }

        // SELECT FOR UPDATE — dos retiros simultáneos no pueden dejar saldo negativo
        Account account = accountOutputPort.findByAccountNumberForUpdate(command.accountNumber())
                .orElseThrow(() -> new AccountNotFoundException(command.accountNumber()));

        Money amount = Money.of(command.amount());

        // Valida: cuenta ACTIVE + monto > 0 + fondos suficientes (reglas en la entidad)
        account.withdraw(amount);
        accountOutputPort.save(account);

        Transaction transaction = Transaction.ofWithdrawal(
                command.reference(), account.getId(), amount, account.getBalance(), command.description()
        );
        return transactionOutputPort.save(transaction);
    }
}
