package com.novobanco.transaction.infrastructure.adapter.input.rest;

import com.novobanco.transaction.application.port.input.CreateAccountUseCase;
import com.novobanco.transaction.application.port.input.GetAccountUseCase;
import com.novobanco.transaction.application.port.input.command.CreateAccountCommand;
import com.novobanco.transaction.infrastructure.adapter.input.rest.dto.request.CreateAccountRequest;
import com.novobanco.transaction.infrastructure.adapter.input.rest.dto.response.AccountResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        var command = new CreateAccountCommand(request.customerId(), request.type());
        return AccountResponse.from(createAccountUseCase.createAccount(command));
    }

    @GetMapping("/{accountNumber}")
    public AccountResponse getByAccountNumber(@PathVariable String accountNumber) {
        return AccountResponse.from(getAccountUseCase.getByAccountNumber(accountNumber));
    }

    @GetMapping("/customer/{customerId}")
    public List<AccountResponse> getByCustomer(@PathVariable Long customerId) {
        return getAccountUseCase.getByCustomerId(customerId).stream()
                .map(AccountResponse::from)
                .toList();
    }
}
