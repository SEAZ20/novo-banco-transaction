package com.novobanco.transaction.infrastructure.adapter.input.exception;

public record ErrorResponse(int status, String code, String detail) {}
