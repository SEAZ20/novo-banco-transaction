package com.novobanco.transaction.application.port.input.command;

public record CreateCustomerCommand(String name, String email, String documentNumber) {}
