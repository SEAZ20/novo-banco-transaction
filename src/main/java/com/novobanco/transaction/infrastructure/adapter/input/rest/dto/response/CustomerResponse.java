package com.novobanco.transaction.infrastructure.adapter.input.rest.dto.response;

import com.novobanco.transaction.domain.model.Customer;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String name,
        String email,
        String documentNumber,
        LocalDateTime createdAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getDocumentNumber(),
                customer.getCreatedAt()
        );
    }
}
