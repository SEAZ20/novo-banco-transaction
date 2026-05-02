package com.novobanco.transaction.infrastructure.adapter.output.persistence.mapper;

import com.novobanco.transaction.domain.model.Money;
import com.novobanco.transaction.domain.model.Transaction;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.entity.TransactionJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "amount", target = "amount", qualifiedByName = "moneyToBigDecimal")
    @Mapping(source = "balanceAfter", target = "balanceAfter", qualifiedByName = "moneyToBigDecimal")
    TransactionJpaEntity toJpaEntity(Transaction transaction);

    @Mapping(source = "amount", target = "amount", qualifiedByName = "bigDecimalToMoney")
    @Mapping(source = "balanceAfter", target = "balanceAfter", qualifiedByName = "bigDecimalToMoney")
    Transaction toDomain(TransactionJpaEntity entity);

    @org.mapstruct.Named("moneyToBigDecimal")
    default BigDecimal moneyToBigDecimal(Money money) {
        return money == null ? BigDecimal.ZERO : money.value();
    }

    @org.mapstruct.Named("bigDecimalToMoney")
    default Money bigDecimalToMoney(BigDecimal value) {
        return value == null ? Money.ZERO : Money.of(value);
    }
}
