package com.novobanco.transaction.application.service;

import com.novobanco.transaction.application.port.output.TransactionOutputPort;
import com.novobanco.transaction.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FailedTransactionRecorder {

    private final TransactionOutputPort transactionOutputPort;

    // REQUIRES_NEW: abre su propia transacción independiente.
    // Aunque la transacción exterior haga rollback, este registro FAILED
    // ya quedó commiteado y persiste en base de datos.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction record(Transaction transaction) {
        return transactionOutputPort.save(transaction);
    }
}
