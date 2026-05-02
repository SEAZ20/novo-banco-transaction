package com.novobanco.transaction.infrastructure.adapter.input.rest;

import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.exception.CustomerAlreadyExistsException;
import com.novobanco.transaction.domain.exception.CustomerNotFoundException;
import com.novobanco.transaction.domain.exception.DuplicateTransactionException;
import com.novobanco.transaction.domain.exception.InactiveAccountException;
import com.novobanco.transaction.domain.exception.InsufficientFundsException;
import com.novobanco.transaction.domain.exception.InvalidAmountException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================================
    // 400 — Bad Request
    // =========================================================================

    @ExceptionHandler(InvalidAmountException.class)
    public ProblemDetail handleInvalidAmount(InvalidAmountException ex) {
        return problem(HttpStatus.BAD_REQUEST, "monto-invalido", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return problem(HttpStatus.BAD_REQUEST, "validacion-fallida", detail);
    }

    // =========================================================================
    // 404 — Not Found
    // =========================================================================

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFound(AccountNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "cuenta-no-encontrada", ex.getMessage());
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleCustomerNotFound(CustomerNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "cliente-no-encontrado", ex.getMessage());
    }

    // =========================================================================
    // 409 — Conflict
    // =========================================================================

    @ExceptionHandler(DuplicateTransactionException.class)
    public ProblemDetail handleDuplicate(DuplicateTransactionException ex) {
        return problem(HttpStatus.CONFLICT, "transaccion-duplicada", ex.getMessage());
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ProblemDetail handleCustomerExists(CustomerAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "cliente-duplicado", ex.getMessage());
    }

    // =========================================================================
    // 422 — Unprocessable Entity (reglas de negocio bancario)
    // =========================================================================

    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "fondos-insuficientes", ex.getMessage());
    }

    @ExceptionHandler(InactiveAccountException.class)
    public ProblemDetail handleInactiveAccount(InactiveAccountException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "cuenta-inactiva", ex.getMessage());
    }

    // =========================================================================
    // 500 — Fallback
    // =========================================================================

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "error-interno",
                "Ocurrió un error inesperado. Contacte a soporte.");
    }

    // =========================================================================

    private ProblemDetail problem(HttpStatus status, String type, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create("https://novobanco.com/errores/" + type));
        return pd;
    }
}
