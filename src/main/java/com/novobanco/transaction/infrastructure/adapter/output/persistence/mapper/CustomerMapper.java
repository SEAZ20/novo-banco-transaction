package com.novobanco.transaction.infrastructure.adapter.output.persistence.mapper;

import com.novobanco.transaction.domain.model.Customer;
import com.novobanco.transaction.infrastructure.adapter.output.persistence.entity.CustomerJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerJpaEntity toJpaEntity(Customer customer);
    Customer toDomain(CustomerJpaEntity entity);
}
