package com.novobanco.transaction.infrastructure.adapter.output.persistence;

import com.novobanco.transaction.domain.model.*;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.entity.TransactionJpaEntity;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.mapper.TransactionMapper;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.repository.TransactionJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionPersistenceAdapterTest {

    @Mock TransactionJpaRepository repository;
    @Mock TransactionMapper mapper;

    @InjectMocks
    TransactionPersistenceAdapter adapter;

    private TransactionJpaEntity entity(String ref) {
        TransactionJpaEntity e = new TransactionJpaEntity();
        e.setId(1L);
        e.setReference(ref);
        e.setAccountId(1L);
        e.setType(TransactionType.DEPOSIT);
        e.setAmount(new BigDecimal("500.00"));
        e.setBalanceAfter(new BigDecimal("500.00"));
        e.setStatus(TransactionStatus.SUCCESS);
        e.setCreatedAt(LocalDateTime.now());
        return e;
    }

    private Transaction domain(String ref) {
        return Transaction.ofDeposit(ref, 1L, Money.of("500.00"), Money.of("500.00"), "Test");
    }

    @Test
    void save_mapsAndPersistsTransaction() {
        Transaction tx = domain("REF-001");
        TransactionJpaEntity entity = entity("REF-001");

        given(mapper.toJpaEntity(tx)).willReturn(entity);
        given(repository.save(entity)).willReturn(entity);
        given(mapper.toDomain(entity)).willReturn(tx);

        Transaction result = adapter.save(tx);

        assertThat(result.getReference()).isEqualTo("REF-001");
        verify(repository).save(entity);
    }

    @Test
    void findByAccountId_returnsPagedResult() {
        TransactionJpaEntity entity = entity("REF-001");
        Transaction domain = domain("REF-001");
        PageImpl<TransactionJpaEntity> page = new PageImpl<>(List.of(entity));

        given(repository.findByAccountIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .willReturn(page);
        given(mapper.toDomain(entity)).willReturn(domain);

        PagedResult<Transaction> result = adapter.findByAccountId(1L, PageRequest.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getReference()).isEqualTo("REF-001");
    }

    @Test
    void findByReference_found_returnsDomain() {
        TransactionJpaEntity entity = entity("REF-001");
        Transaction domain = domain("REF-001");

        given(repository.findByReference("REF-001")).willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        Optional<Transaction> result = adapter.findByReference("REF-001");

        assertThat(result).isPresent();
        assertThat(result.get().getReference()).isEqualTo("REF-001");
    }

    @Test
    void findByReference_notFound_returnsEmpty() {
        given(repository.findByReference("NOPE")).willReturn(Optional.empty());

        assertThat(adapter.findByReference("NOPE")).isEmpty();
    }

    @Test
    void existsByReference_delegatesToRepository() {
        given(repository.existsByReference("REF-001")).willReturn(true);

        assertThat(adapter.existsByReference("REF-001")).isTrue();
    }
}
