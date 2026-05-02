package com.novobanco.transaction.infrastructure.adapter.input.rest.dto.request;

import com.novobanco.transaction.domain.model.AccountType;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(

        @NotNull(message = "El id del cliente es obligatorio")
        Long customerId,

        @NotNull(message = "El tipo de cuenta es obligatorio (SAVINGS o CHECKING)")
        AccountType type
) {}
