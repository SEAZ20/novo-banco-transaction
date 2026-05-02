package com.novobanco.transaction.application.service;

import com.novobanco.transaction.application.port.output.AccountOutputPort;
import com.novobanco.transaction.application.port.output.TransactionOutputPort;
import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TransactionHistoryServiceTest {

    @Mock AccountOutputPort accountOutputPort;
    @Mock TransactionOutputPort transactionOutputPort;

    @InjectMocks
    TransactionHistoryService service;

    private static final String ACCOUNT_NUMBER = "NB0000000001";
    private static final Account ACCOUNT = new Account(
            1L, ACCOUNT_NUMBER, 10L, AccountType.SAVINGS, "USD",
            Money.ZERO, AccountStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());

    @Test
    void getHistory_accountFound_returnsPagedResult() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Transaction> transactions = List.of(
                Transaction.ofDeposit("REF-1", 1L, Money.of("100"), Money.of("100"), "dep1"),
                Transaction.ofDeposit("REF-2", 1L, Money.of("200"), Money.of("300"), "dep2")
        );
        PagedResult<Transaction> expected = PagedResult.of(transactions, 0, 10, 2L);

        given(accountOutputPort.findByAccountNumber(ACCOUNT_NUMBER)).willReturn(Optional.of(ACCOUNT));
        given(transactionOutputPort.findByAccountId(1L, pageRequest)).willReturn(expected);

        PagedResult<Transaction> result = service.getHistory(ACCOUNT_NUMBER, pageRequest);

        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.content()).hasSize(2);
        assertThat(result.page()).isEqualTo(0);
    }

    @Test
    void getHistory_emptyHistory_returnsEmptyPage() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        PagedResult<Transaction> empty = PagedResult.of(List.of(), 0, 10, 0L);

        given(accountOutputPort.findByAccountNumber(ACCOUNT_NUMBER)).willReturn(Optional.of(ACCOUNT));
        given(transactionOutputPort.findByAccountId(1L, pageRequest)).willReturn(empty);

        PagedResult<Transaction> result = service.getHistory(ACCOUNT_NUMBER, pageRequest);

        assertThat(result.totalElements()).isZero();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void getHistory_accountNotFound_throwsAccountNotFoundException() {
        given(accountOutputPort.findByAccountNumber(ACCOUNT_NUMBER)).willReturn(Optional.empty());

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() -> service.getHistory(ACCOUNT_NUMBER, PageRequest.of(0, 10)));
    }
}
