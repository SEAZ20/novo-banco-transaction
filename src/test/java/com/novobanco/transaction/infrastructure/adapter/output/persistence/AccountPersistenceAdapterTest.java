package com.novobanco.transaction.infrastructure.adapter.output.persistence;

import com.novobanco.transaction.domain.model.*;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.entity.AccountJpaEntity;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.mapper.AccountMapper;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.repository.AccountJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountPersistenceAdapterTest {

    @Mock AccountJpaRepository repository;
    @Mock AccountMapper mapper;

    @InjectMocks
    AccountPersistenceAdapter adapter;

    private AccountJpaEntity entity(String number) {
        AccountJpaEntity e = new AccountJpaEntity();
        e.setId(1L);
        e.setAccountNumber(number);
        e.setCustomerId(10L);
        e.setType(AccountType.SAVINGS);
        e.setCurrency("USD");
        e.setBalance(BigDecimal.ZERO);
        e.setStatus(AccountStatus.ACTIVE);
        e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());
        return e;
    }

    private Account domain(String number) {
        return new Account(1L, number, 10L, AccountType.SAVINGS, "USD",
                Money.ZERO, AccountStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void save_mapsAndPersistsAccount() {
        Account account = domain("NB0000000001");
        AccountJpaEntity entity = entity("NB0000000001");

        given(mapper.toJpaEntity(account)).willReturn(entity);
        given(repository.save(entity)).willReturn(entity);
        given(mapper.toDomain(entity)).willReturn(account);

        Account result = adapter.save(account);

        assertThat(result.getAccountNumber()).isEqualTo("NB0000000001");
        verify(repository).save(entity);
    }

    @Test
    void findByAccountNumber_found_returnsDomain() {
        AccountJpaEntity entity = entity("NB0000000001");
        Account domain = domain("NB0000000001");

        given(repository.findByAccountNumber("NB0000000001")).willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        Optional<Account> result = adapter.findByAccountNumber("NB0000000001");

        assertThat(result).isPresent();
        assertThat(result.get().getAccountNumber()).isEqualTo("NB0000000001");
    }

    @Test
    void findByAccountNumber_notFound_returnsEmpty() {
        given(repository.findByAccountNumber("NB9999999999")).willReturn(Optional.empty());

        assertThat(adapter.findByAccountNumber("NB9999999999")).isEmpty();
    }

    @Test
    void findByAccountNumberForUpdate_found_returnsDomain() {
        AccountJpaEntity entity = entity("NB0000000001");
        Account domain = domain("NB0000000001");

        given(repository.findByAccountNumberForUpdate("NB0000000001")).willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        Optional<Account> result = adapter.findByAccountNumberForUpdate("NB0000000001");

        assertThat(result).isPresent();
    }

    @Test
    void findByCustomerId_returnsMappedList() {
        AccountJpaEntity e1 = entity("NB0000000001");
        AccountJpaEntity e2 = entity("NB0000000002");
        e2.setId(2L);
        Account d1 = domain("NB0000000001");
        Account d2 = domain("NB0000000002");

        given(repository.findByCustomerId(10L)).willReturn(List.of(e1, e2));
        given(mapper.toDomain(e1)).willReturn(d1);
        given(mapper.toDomain(e2)).willReturn(d2);

        List<Account> result = adapter.findByCustomerId(10L);

        assertThat(result).hasSize(2);
    }
}
