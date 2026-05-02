package com.novobanco.transaction.infrastructure.adapter.output.persistence.repository;

import com.novobanco.transaction.infrastructure.adapter.output.persistence.entity.AccountJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, Long> {

    Optional<AccountJpaEntity> findByAccountNumber(String accountNumber);

    List<AccountJpaEntity> findByCustomerId(Long customerId);

    // SELECT ... FOR UPDATE — previene condiciones de carrera en retiros/depósitos concurrentes
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountJpaEntity a WHERE a.accountNumber = :accountNumber")
    Optional<AccountJpaEntity> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
}
