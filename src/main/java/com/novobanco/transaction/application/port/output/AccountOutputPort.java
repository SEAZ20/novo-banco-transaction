package com.novobanco.transaction.application.port.output;

import com.novobanco.transaction.domain.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountOutputPort {
    Account save(Account account);
    Optional<Account> findByAccountNumber(String accountNumber);
    // SELECT FOR UPDATE: bloquea la fila para evitar escrituras concurrentes sobre el saldo
    Optional<Account> findByAccountNumberForUpdate(String accountNumber);
    List<Account> findByCustomerId(Long customerId);
}
