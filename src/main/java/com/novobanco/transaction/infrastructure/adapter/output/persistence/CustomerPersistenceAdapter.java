package com.novobanco.transaction.infrastructure.adapter.output.persistence;

import com.novobanco.transaction.application.port.output.CustomerOutputPort;
import com.novobanco.transaction.domain.model.Customer;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.mapper.CustomerMapper;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerOutputPort {

    private final CustomerJpaRepository repository;
    private final CustomerMapper mapper;

    @Override
    public Customer save(Customer customer) {
        return mapper.toDomain(repository.save(mapper.toJpaEntity(customer)));
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return repository.existsByDocumentNumber(documentNumber);
    }
}
