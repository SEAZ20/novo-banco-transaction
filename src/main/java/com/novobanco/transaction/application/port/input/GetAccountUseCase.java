package com.novobanco.transaction.application.port.input;

import com.novobanco.transaction.domain.model.Account;

import java.util.List;

public interface GetAccountUseCase {
    Account getByAccountNumber(String accountNumber);
    List<Account> getByCustomerId(Long customerId);
}
