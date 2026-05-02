package com.novobanco.transaction.application.service;

import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.exception.CustomerNotFoundException;
import com.novobanco.transaction.domain.model.Account;
import com.novobanco.transaction.application.port.input.CreateAccountUseCase;
import com.novobanco.transaction.application.port.input.GetAccountUseCase;
import com.novobanco.transaction.application.port.input.command.CreateAccountCommand;
import com.novobanco.transaction.application.port.output.AccountOutputPort;
import com.novobanco.transaction.application.port.output.CustomerOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService implements CreateAccountUseCase, GetAccountUseCase {

    private final AccountOutputPort accountOutputPort;
    private final CustomerOutputPort customerOutputPort;

    @Override
    @Transactional
    public Account createAccount(CreateAccountCommand command) {
        customerOutputPort.findById(command.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));

        String accountNumber = generateAccountNumber();
        Account account = Account.create(accountNumber, command.customerId(), command.type());
        return accountOutputPort.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Account getByAccountNumber(String accountNumber) {
        return accountOutputPort.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getByCustomerId(Long customerId) {
        customerOutputPort.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        return accountOutputPort.findByCustomerId(customerId);
    }

    // En producción: output port respaldado por una secuencia Oracle (SEQ_ACCOUNT_NUMBER)
    private String generateAccountNumber() {
        long number = Math.abs(UUID.randomUUID().getMostSignificantBits() % 10_000_000_000L);
        return "NB" + String.format("%010d", number);
    }
}
