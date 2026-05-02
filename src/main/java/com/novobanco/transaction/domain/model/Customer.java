package com.novobanco.transaction.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Customer {

    private Long id;
    private final String name;
    private final String email;
    private final String documentNumber;
    private final LocalDateTime createdAt;

    public Customer(Long id, String name, String email, String documentNumber, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.documentNumber = documentNumber;
        this.createdAt = createdAt;
    }

    public static Customer create(String name, String email, String documentNumber) {
        return new Customer(null, name, email, documentNumber, LocalDateTime.now());
    }

    public void assignId(Long id) {
        if (this.id != null) throw new IllegalStateException("El id del cliente ya fue asignado");
        this.id = id;
    }
}
