package com.novobanco.transaction.domain.port.output;

import com.novobanco.transaction.domain.model.PageRequest;
import com.novobanco.transaction.domain.model.PagedResult;
import com.novobanco.transaction.domain.model.Transaction;

import java.util.Optional;

public interface TransactionOutputPort {
    Transaction save(Transaction transaction);
    PagedResult<Transaction> findByAccountId(Long accountId, PageRequest pageRequest);
    Optional<Transaction> findByReference(String reference);
    boolean existsByReference(String reference);
}
