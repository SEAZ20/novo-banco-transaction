package com.novobanco.transaction.application.port.input.command;

import com.novobanco.transaction.domain.model.Transaction;

public record TransferResult(Transaction debit, Transaction credit) {}
