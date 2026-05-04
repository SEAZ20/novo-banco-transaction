package com.novobanco.transaction.infrastructure.adapter.input.rest;

import com.novobanco.transaction.application.port.input.*;
import com.novobanco.transaction.application.port.input.command.*;
import com.novobanco.transaction.domain.exception.DuplicateTransactionException;
import com.novobanco.transaction.domain.exception.InsufficientFundsException;
import com.novobanco.transaction.domain.model.*;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock DepositUseCase depositUseCase;
    @Mock WithdrawUseCase withdrawUseCase;
    @Mock TransferUseCase transferUseCase;
    @Mock GetTransactionHistoryUseCase historyUseCase;

    @InjectMocks TransactionController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Transaction depositTx() {
        return Transaction.ofDeposit("REF-001", 1L, Money.of("500.00"), Money.of("500.00"), "Test");
    }

    // -------------------------------------------------------------------------
    // Deposit
    // -------------------------------------------------------------------------

    @Test
    void deposit_success_returns201() throws Exception {
        given(depositUseCase.deposit(any(DepositCommand.class))).willReturn(depositTx());

        mockMvc.perform(post("/api/v1/accounts/NB0000000001/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":500.00,"description":"Test","reference":"REF-001"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(500.00));
    }

    @Test
    void deposit_duplicateReference_returns409() throws Exception {
        given(depositUseCase.deposit(any()))
                .willThrow(new DuplicateTransactionException("REF-001"));

        mockMvc.perform(post("/api/v1/accounts/NB0000000001/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":500.00,"description":"Test","reference":"REF-001"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // -------------------------------------------------------------------------
    // Withdraw
    // -------------------------------------------------------------------------

    @Test
    void withdraw_success_returns201() throws Exception {
        Transaction tx = Transaction.ofWithdrawal("REF-WD", 1L, Money.of("200.00"), Money.of("300.00"), "Test");
        given(withdrawUseCase.withdraw(any(WithdrawCommand.class))).willReturn(tx);

        mockMvc.perform(post("/api/v1/accounts/NB0000000001/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":200.00,"description":"Test","reference":"REF-WD"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.balanceAfter").value(300.00));
    }

    @Test
    void withdraw_insufficientFunds_returns422() throws Exception {
        given(withdrawUseCase.withdraw(any()))
                .willThrow(new InsufficientFundsException("NB0000000001", Money.of("500"), Money.of("10")));

        mockMvc.perform(post("/api/v1/accounts/NB0000000001/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":500.00,"description":"Test","reference":"REF-WD"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    // -------------------------------------------------------------------------
    // Transfer
    // -------------------------------------------------------------------------

    @Test
    void transfer_success_returns201() throws Exception {
        Transaction debit  = Transaction.ofTransferDebit("REF-TR", 1L, 2L,
                Money.of("300.00"), Money.of("700.00"), "Test");
        Transaction credit = Transaction.ofTransferCredit("REF-CR", 2L, 1L,
                Money.of("300.00"), Money.of("300.00"), "Test");
        given(transferUseCase.transfer(any(TransferCommand.class)))
                .willReturn(new TransferResult(debit, credit));

        mockMvc.perform(post("/api/v1/accounts/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceAccountNumber":"NB0000000001","targetAccountNumber":"NB0000000002",
                                 "amount":300.00,"description":"Test","reference":"REF-TR"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.debit.type").value("TRANSFER_DEBIT"))
                .andExpect(jsonPath("$.credit.type").value("TRANSFER_CREDIT"))
                .andExpect(jsonPath("$.debit.amount").value(300.00));
    }

    // -------------------------------------------------------------------------
    // History
    // -------------------------------------------------------------------------

    @Test
    void getHistory_success_returns200WithPagedResult() throws Exception {
        List<Transaction> txs = List.of(depositTx());
        PagedResult<Transaction> paged = PagedResult.of(txs, 0, 20, 1L);
        given(historyUseCase.getHistory(any(), any(PageRequest.class))).willReturn(paged);

        mockMvc.perform(get("/api/v1/accounts/NB0000000001/transactions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }
}
