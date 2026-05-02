package com.novobanco.transaction.infrastructure.adapter.output.persistence;

import com.novobanco.transaction.application.port.output.AccountOutputPort;
import com.novobanco.transaction.domain.model.Account;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.mapper.AccountMapper;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountPersistenceAdapter implements AccountOutputPort {

    private final AccountJpaRepository repository;
    private final AccountMapper mapper;

    @Override
    public Account save(Account account) {
        return mapper.toDomain(repository.save(mapper.toJpaEntity(account)));
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNumberForUpdate(String accountNumber) {
        return repository.findByAccountNumberForUpdate(accountNumber).map(mapper::toDomain);
    }

    @Override
    public List<Account> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
