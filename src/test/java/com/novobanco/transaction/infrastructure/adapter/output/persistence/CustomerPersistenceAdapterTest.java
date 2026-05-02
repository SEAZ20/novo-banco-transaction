package com.novobanco.transaction.infrastructure.adapter.output.persistence;

import com.novobanco.transaction.domain.model.Customer;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.entity.CustomerJpaEntity;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.mapper.CustomerMapper;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.repository.CustomerJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerPersistenceAdapterTest {

    @Mock CustomerJpaRepository repository;
    @Mock CustomerMapper mapper;

    @InjectMocks
    CustomerPersistenceAdapter adapter;

    private CustomerJpaEntity entity() {
        CustomerJpaEntity e = new CustomerJpaEntity();
        e.setId(1L);
        e.setName("Test");
        e.setEmail("test@mail.com");
        e.setDocumentNumber("DOC001");
        e.setCreatedAt(LocalDateTime.now());
        return e;
    }

    private Customer domain() {
        return new Customer(1L, "Test", "test@mail.com", "DOC001", LocalDateTime.now());
    }

    @Test
    void save_mapsAndPersistsCustomer() {
        Customer customer = domain();
        CustomerJpaEntity entity = entity();

        given(mapper.toJpaEntity(customer)).willReturn(entity);
        given(repository.save(entity)).willReturn(entity);
        given(mapper.toDomain(entity)).willReturn(customer);

        Customer result = adapter.save(customer);

        assertThat(result.getId()).isEqualTo(1L);
        verify(repository).save(entity);
    }

    @Test
    void findById_found_returnsDomainCustomer() {
        CustomerJpaEntity entity = entity();
        Customer domain = domain();

        given(repository.findById(1L)).willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        Optional<Customer> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@mail.com");
    }

    @Test
    void findById_notFound_returnsEmpty() {
        given(repository.findById(99L)).willReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void existsByEmail_delegatesToRepository() {
        given(repository.existsByEmail("test@mail.com")).willReturn(true);

        assertThat(adapter.existsByEmail("test@mail.com")).isTrue();
    }

    @Test
    void existsByDocumentNumber_delegatesToRepository() {
        given(repository.existsByDocumentNumber("DOC001")).willReturn(false);

        assertThat(adapter.existsByDocumentNumber("DOC001")).isFalse();
    }
}
