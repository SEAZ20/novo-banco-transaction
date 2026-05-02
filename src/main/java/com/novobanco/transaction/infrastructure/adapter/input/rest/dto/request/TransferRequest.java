package com.novobanco.transaction.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferRequest(

        @NotBlank(message = "La cuenta origen es obligatoria")
        String sourceAccountNumber,

        @NotBlank(message = "La cuenta destino es obligatoria")
        String targetAccountNumber,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal amount,

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String description,

        @NotBlank(message = "La referencia es obligatoria (UUID v4 generado por el cliente)")
        @Size(max = 50, message = "La referencia no puede superar 50 caracteres")
        String reference
) {}
