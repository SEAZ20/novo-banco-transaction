package com.novobanco.transaction.application.service;

import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.model.Account;
import com.novobanco.transaction.domain.model.PageRequest;
import com.novobanco.transaction.domain.model.PagedResult;
import com.novobanco.transaction.domain.model.Transaction;
import com.novobanco.transaction.application.port.input.GetTransactionHistoryUseCase;
import com.novobanco.transaction.application.port.output.AccountOutputPort;
import com.novobanco.transaction.application.port.output.TransactionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionHistoryService implements GetTransactionHistoryUseCase {

    private final AccountOutputPort accountOutputPort;
    private final TransactionOutputPort transactionOutputPort;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Transaction> getHistory(String accountNumber, PageRequest pageRequest) {
        Account account = accountOutputPort.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        return transactionOutputPort.findByAccountId(account.getId(), pageRequest);
    }
}
