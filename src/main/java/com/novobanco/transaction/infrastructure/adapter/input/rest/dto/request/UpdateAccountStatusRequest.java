package com.novobanco.transaction.infrastructure.adapter.input.rest.dto.request;

import com.novobanco.transaction.domain.model.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(

        @NotNull(message = "El estado es obligatorio")
        AccountStatus status
) {}
