package com.novobanco.transaction.application.service;

import com.novobanco.transaction.application.port.input.command.CreateCustomerCommand;
import com.novobanco.transaction.application.port.output.CustomerOutputPort;
import com.novobanco.transaction.domain.exception.CustomerAlreadyExistsException;
import com.novobanco.transaction.domain.model.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    CustomerOutputPort customerOutputPort;

    @InjectMocks
    CustomerService service;

    private static final CreateCustomerCommand COMMAND =
            new CreateCustomerCommand("Ana García", "ana@test.com", "12345678");

    @Test
    void createCustomer_success_returnsSavedCustomer() {
        Customer saved = new Customer(1L, "Ana García", "ana@test.com", "12345678", LocalDateTime.now());

        given(customerOutputPort.existsByEmail(COMMAND.email())).willReturn(false);
        given(customerOutputPort.existsByDocumentNumber(COMMAND.documentNumber())).willReturn(false);
        given(customerOutputPort.save(any(Customer.class))).willReturn(saved);

        Customer result = service.createCustomer(COMMAND);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("ana@test.com");
        verify(customerOutputPort).save(any(Customer.class));
    }

    @Test
    void createCustomer_duplicateEmail_throwsCustomerAlreadyExistsException() {
        given(customerOutputPort.existsByEmail(COMMAND.email())).willReturn(true);

        assertThatExceptionOfType(CustomerAlreadyExistsException.class)
                .isThrownBy(() -> service.createCustomer(COMMAND));

        verify(customerOutputPort, never()).save(any());
    }

    @Test
    void createCustomer_duplicateDocument_throwsCustomerAlreadyExistsException() {
        given(customerOutputPort.existsByEmail(COMMAND.email())).willReturn(false);
        given(customerOutputPort.existsByDocumentNumber(COMMAND.documentNumber())).willReturn(true);

        assertThatExceptionOfType(CustomerAlreadyExistsException.class)
                .isThrownBy(() -> service.createCustomer(COMMAND));

        verify(customerOutputPort, never()).save(any());
    }
}
