package com.novobanco.transaction.application.service;

import com.novobanco.transaction.domain.exception.CustomerAlreadyExistsException;
import com.novobanco.transaction.domain.model.Customer;
import com.novobanco.transaction.application.port.input.CreateCustomerUseCase;
import com.novobanco.transaction.application.port.input.command.CreateCustomerCommand;
import com.novobanco.transaction.application.port.output.CustomerOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService implements CreateCustomerUseCase {

    private final CustomerOutputPort customerOutputPort;

    @Override
    @Transactional
    public Customer createCustomer(CreateCustomerCommand command) {
        if (customerOutputPort.existsByEmail(command.email())) {
            throw new CustomerAlreadyExistsException("email", command.email());
        }
        if (customerOutputPort.existsByDocumentNumber(command.documentNumber())) {
            throw new CustomerAlreadyExistsException("documento", command.documentNumber());
        }

        Customer customer = Customer.create(command.name(), command.email(), command.documentNumber());
        return customerOutputPort.save(customer);
    }
}
