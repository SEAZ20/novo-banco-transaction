package com.novobanco.transaction.infrastructure.adapter.input.exception;

import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.exception.CustomerAlreadyExistsException;
import com.novobanco.transaction.domain.exception.CustomerNotFoundException;
import com.novobanco.transaction.domain.exception.DuplicateTransactionException;
import com.novobanco.transaction.domain.exception.InactiveAccountException;
import com.novobanco.transaction.domain.exception.InsufficientFundsException;
import com.novobanco.transaction.domain.exception.InvalidAmountException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================================
    // 400 — Bad Request
    // =========================================================================

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmount(InvalidAmountException ex) {
        return error(HttpStatus.BAD_REQUEST, "monto-invalido", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, "validacion-fallida", detail);
    }

    // =========================================================================
    // 404 — Not Found
    // =========================================================================

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "cuenta-no-encontrada", ex.getMessage());
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "cliente-no-encontrado", ex.getMessage());
    }

    // =========================================================================
    // 409 — Conflict
    // =========================================================================

    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateTransactionException ex) {
        return error(HttpStatus.CONFLICT, "transaccion-duplicada", ex.getMessage());
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCustomerExists(CustomerAlreadyExistsException ex) {
        return error(HttpStatus.CONFLICT, "cliente-duplicado", ex.getMessage());
    }

    // =========================================================================
    // 422 — Unprocessable Entity (reglas de negocio bancario)
    // =========================================================================

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, "fondos-insuficientes", ex.getMessage());
    }

    @ExceptionHandler(InactiveAccountException.class)
    public ResponseEntity<ErrorResponse> handleInactiveAccount(InactiveAccountException ex) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, "cuenta-inactiva", ex.getMessage());
    }

    // =========================================================================
    // 500 — Fallback
    // =========================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "error-interno",
                "Ocurrió un error inesperado. Contacte a soporte.");
    }

    // =========================================================================

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String detail) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), code, detail));
    }
}
