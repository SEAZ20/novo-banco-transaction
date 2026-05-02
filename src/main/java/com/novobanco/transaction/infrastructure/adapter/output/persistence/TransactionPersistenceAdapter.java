package com.novobanco.transaction.infrastructure.adapter.output.persistence;

import com.novobanco.transaction.application.port.output.TransactionOutputPort;
import com.novobanco.transaction.domain.model.PageRequest;
import com.novobanco.transaction.domain.model.PagedResult;
import com.novobanco.transaction.domain.model.Transaction;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.mapper.TransactionMapper;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.repository.TransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionOutputPort {

    private final TransactionJpaRepository repository;
    private final TransactionMapper mapper;

    @Override
    public Transaction save(Transaction transaction) {
        return mapper.toDomain(repository.save(mapper.toJpaEntity(transaction)));
    }

    @Override
    public PagedResult<Transaction> findByAccountId(Long accountId, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPageable =
                org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());
        Page<Transaction> page = repository
                .findByAccountIdOrderByCreatedAtDesc(accountId, springPageable)
                .map(mapper::toDomain);

        return PagedResult.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public Optional<Transaction> findByReference(String reference) {
        return repository.findByReference(reference).map(mapper::toDomain);
    }

    @Override
    public boolean existsByReference(String reference) {
        return repository.existsByReference(reference);
    }
}
