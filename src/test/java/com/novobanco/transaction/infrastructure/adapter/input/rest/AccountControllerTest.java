package com.novobanco.transaction.infrastructure.adapter.input.rest;

import com.novobanco.transaction.application.port.input.CreateAccountUseCase;
import com.novobanco.transaction.application.port.input.GetAccountUseCase;
import com.novobanco.transaction.application.port.input.command.CreateAccountCommand;
import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.exception.CustomerNotFoundException;
import com.novobanco.transaction.domain.model.*;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock CreateAccountUseCase createAccountUseCase;
    @Mock GetAccountUseCase getAccountUseCase;

    @InjectMocks AccountController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Account activeAccount(String number) {
        return new Account(1L, number, 10L, AccountType.SAVINGS, "USD",
                Money.ZERO, AccountStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void createAccount_success_returns201() throws Exception {
        given(createAccountUseCase.createAccount(any(CreateAccountCommand.class)))
                .willReturn(activeAccount("NB0000000001"));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":10,"type":"SAVINGS"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("NB0000000001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void createAccount_customerNotFound_returns404() throws Exception {
        given(createAccountUseCase.createAccount(any()))
                .willThrow(new CustomerNotFoundException(10L));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":10,"type":"SAVINGS"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getByAccountNumber_found_returns200() throws Exception {
        given(getAccountUseCase.getByAccountNumber("NB0000000001"))
                .willReturn(activeAccount("NB0000000001"));

        mockMvc.perform(get("/api/v1/accounts/NB0000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("NB0000000001"))
                .andExpect(jsonPath("$.type").value("SAVINGS"));
    }

    @Test
    void getByAccountNumber_notFound_returns404() throws Exception {
        given(getAccountUseCase.getByAccountNumber("NB9999999999"))
                .willThrow(new AccountNotFoundException("NB9999999999"));

        mockMvc.perform(get("/api/v1/accounts/NB9999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getByCustomerId_returns200WithList() throws Exception {
        given(getAccountUseCase.getByCustomerId(10L))
                .willReturn(List.of(activeAccount("NB0000000001"), activeAccount("NB0000000002")));

        mockMvc.perform(get("/api/v1/accounts/customer/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
