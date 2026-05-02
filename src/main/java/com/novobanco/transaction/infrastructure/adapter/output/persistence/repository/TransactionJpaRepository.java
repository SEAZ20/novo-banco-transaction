package com.novobanco.transaction.infrastructure.adapter.output.persistence.repository;

import com.novobanco.transaction.infrastructure.adapter.output.persistence.entity.TransactionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {

    // Usa idx_transactions_account_created_at — historial paginado descendente
    Page<TransactionJpaEntity> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    boolean existsByReference(String reference);

    Optional<TransactionJpaEntity> findByReference(String reference);
}
