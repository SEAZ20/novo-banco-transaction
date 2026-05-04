package com.novobanco.transaction.infrastructure.adapter.input.rest;

import com.novobanco.transaction.application.port.input.CreateCustomerUseCase;
import com.novobanco.transaction.application.port.input.command.CreateCustomerCommand;
import com.novobanco.transaction.domain.exception.CustomerAlreadyExistsException;
import com.novobanco.transaction.domain.model.Customer;
import com.novobanco.transaction.infrastructure.adapter.input.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock CreateCustomerUseCase createCustomerUseCase;

    @InjectMocks CustomerController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static final String VALID_BODY = """
            {"name":"Ana García","email":"ana@test.com","documentNumber":"12345678"}
            """;

    @Test
    void createCustomer_success_returns201() throws Exception {
        Customer saved = new Customer(1L, "Ana García", "ana@test.com", "12345678", LocalDateTime.now());
        given(createCustomerUseCase.createCustomer(any(CreateCustomerCommand.class))).willReturn(saved);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ana García"))
                .andExpect(jsonPath("$.email").value("ana@test.com"));
    }

    @Test
    void createCustomer_duplicateEmail_returns409() throws Exception {
        given(createCustomerUseCase.createCustomer(any()))
                .willThrow(new CustomerAlreadyExistsException("email", "ana@test.com"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createCustomer_invalidEmail_returns400() throws Exception {
        String body = """
                {"name":"Test","email":"no-es-email","documentNumber":"DOC001"}
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createCustomer_blankName_returns400() throws Exception {
        String body = """
                {"name":"","email":"ok@test.com","documentNumber":"DOC001"}
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
