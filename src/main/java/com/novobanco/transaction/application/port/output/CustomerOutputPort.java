package com.novobanco.transaction.application.port.output;

import com.novobanco.transaction.domain.model.Customer;

import java.util.Optional;

public interface CustomerOutputPort {
    Customer save(Customer customer);
    Optional<Customer> findById(Long id);
    boolean existsByEmail(String email);
    boolean existsByDocumentNumber(String documentNumber);
}
