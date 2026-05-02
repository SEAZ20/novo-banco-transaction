package com.novobanco.transaction.infrastructure.adapter.output.persistence.mapper;

import com.novobanco.transaction.domain.model.Account;
import com.novobanco.transaction.domain.model.Money;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.entity.AccountJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(source = "balance", target = "balance", qualifiedByName = "moneyToBigDecimal")
    AccountJpaEntity toJpaEntity(Account account);

    @Mapping(source = "balance", target = "balance", qualifiedByName = "bigDecimalToMoney")
    Account toDomain(AccountJpaEntity entity);

    @org.mapstruct.Named("moneyToBigDecimal")
    default BigDecimal moneyToBigDecimal(Money money) {
        return money == null ? BigDecimal.ZERO : money.value();
    }

    @org.mapstruct.Named("bigDecimalToMoney")
    default Money bigDecimalToMoney(BigDecimal value) {
        return value == null ? Money.ZERO : Money.of(value);
    }
}
