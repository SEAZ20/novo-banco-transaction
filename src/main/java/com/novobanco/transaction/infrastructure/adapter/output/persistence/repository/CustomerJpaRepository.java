package com.novobanco.transaction.infrastructure.adapter.output.persistence.repository;

import com.novobanco.transaction.infrastructure.adapter.output.persistence.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, Long> {
    boolean existsByEmail(String email);
    boolean existsByDocumentNumber(String documentNumber);
}
