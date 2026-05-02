package com.novobanco.transaction.infrastructure.adapter.input.rest;

import com.novobanco.transaction.application.port.input.DepositUseCase;
import com.novobanco.transaction.application.port.input.GetTransactionHistoryUseCase;
import com.novobanco.transaction.application.port.input.TransferUseCase;
import com.novobanco.transaction.application.port.input.WithdrawUseCase;
import com.novobanco.transaction.application.port.input.command.DepositCommand;
import com.novobanco.transaction.application.port.input.command.TransferCommand;
import com.novobanco.transaction.application.port.input.command.WithdrawCommand;
import com.novobanco.transaction.domain.model.PageRequest;
import com.novobanco.transaction.infrastructure.adapter.input.rest.dto.request.DepositRequest;
import com.novobanco.transaction.infrastructure.adapter.input.rest.dto.request.TransferRequest;
import com.novobanco.transaction.infrastructure.adapter.input.rest.dto.request.WithdrawRequest;
import com.novobanco.transaction.infrastructure.adapter.input.rest.dto.response.PagedResponse;
import com.novobanco.transaction.infrastructure.adapter.input.rest.dto.response.TransactionResponse;
import com.novobanco.transaction.infrastructure.adapter.input.rest.dto.response.TransferResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class TransactionController {

    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final TransferUseCase transferUseCase;
    private final GetTransactionHistoryUseCase historyUseCase;

    @PostMapping("/{accountNumber}/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse deposit(@PathVariable String accountNumber,
                                       @Valid @RequestBody DepositRequest request) {
        var command = new DepositCommand(accountNumber, request.amount(), request.description(), request.reference());
        return TransactionResponse.from(depositUseCase.deposit(command));
    }

    @PostMapping("/{accountNumber}/withdrawals")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse withdraw(@PathVariable String accountNumber,
                                        @Valid @RequestBody WithdrawRequest request) {
        var command = new WithdrawCommand(accountNumber, request.amount(), request.description(), request.reference());
        return TransactionResponse.from(withdrawUseCase.withdraw(command));
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        var command = new TransferCommand(
                request.sourceAccountNumber(), request.targetAccountNumber(),
                request.amount(), request.description(), request.reference()
        );
        return TransferResponse.from(transferUseCase.transfer(command));
    }

    @GetMapping("/{accountNumber}/transactions")
    public PagedResponse<TransactionResponse> getHistory(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageRequest = PageRequest.of(page, size);
        var result = historyUseCase.getHistory(accountNumber, pageRequest);
        return PagedResponse.from(result, TransactionResponse::from);
    }
}
