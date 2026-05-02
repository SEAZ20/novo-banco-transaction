package com.novobanco.transaction.infrastructure.adapter.input.rest;

import com.novobanco.transaction.application.port.input.CreateCustomerUseCase;
import com.novobanco.transaction.application.port.input.command.CreateCustomerCommand;
import com.novobanco.transaction.infrastructure.adapter.input.rest.dto.request.CreateCustomerRequest;
import com.novobanco.transaction.infrastructure.adapter.input.rest.dto.response.CustomerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CreateCustomerRequest request) {
        var command = new CreateCustomerCommand(request.name(), request.email(), request.documentNumber());
        return CustomerResponse.from(createCustomerUseCase.createCustomer(command));
    }
}
