package com.novobanco.transaction.domain.port.input;

import com.novobanco.transaction.domain.port.input.command.TransferCommand;
import com.novobanco.transaction.domain.port.input.command.TransferResult;

public interface TransferUseCase {
    TransferResult transfer(TransferCommand command);
}
