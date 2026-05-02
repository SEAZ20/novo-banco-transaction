package com.novobanco.transaction.domain.port.input;

import com.novobanco.transaction.domain.model.PageRequest;
import com.novobanco.transaction.domain.model.PagedResult;
import com.novobanco.transaction.domain.model.Transaction;

public interface GetTransactionHistoryUseCase {
    PagedResult<Transaction> getHistory(String accountNumber, PageRequest pageRequest);
}
