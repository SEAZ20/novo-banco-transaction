package com.novobanco.transaction.application.port.input;

import com.novobanco.transaction.application.port.input.command.TransferCommand;
import com.novobanco.transaction.application.port.input.command.TransferResult;

public interface TransferUseCase {
    TransferResult transfer(TransferCommand command);
}
