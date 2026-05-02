package com.novobanco.transaction.application.port.input;

import com.novobanco.transaction.domain.model.Customer;
import com.novobanco.transaction.application.port.input.command.CreateCustomerCommand;

public interface CreateCustomerUseCase {
    Customer createCustomer(CreateCustomerCommand command);
}
