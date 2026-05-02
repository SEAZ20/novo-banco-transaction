package com.novobanco.transaction.infrastructure.adapter.input.rest.dto.response;

import com.novobanco.transaction.application.port.input.command.TransferResult;

public record TransferResponse(
        TransactionResponse debit,
        TransactionResponse credit
) {
    public static TransferResponse from(TransferResult result) {
        return new TransferResponse(
                TransactionResponse.from(result.debit()),
                TransactionResponse.from(result.credit())
        );
    }
}
