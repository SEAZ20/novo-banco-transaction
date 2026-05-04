package com.novobanco.transaction.application.service;

import com.novobanco.transaction.application.port.input.command.CreateAccountCommand;
import com.novobanco.transaction.application.port.input.command.UpdateAccountStatusCommand;
import com.novobanco.transaction.application.port.output.AccountOutputPort;
import com.novobanco.transaction.application.port.output.CustomerOutputPort;
import com.novobanco.transaction.domain.exception.AccountNotFoundException;
import com.novobanco.transaction.domain.exception.CustomerNotFoundException;
import com.novobanco.transaction.domain.exception.InactiveAccountException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountOutputPort accountOutputPort;
    @Mock CustomerOutputPort customerOutputPort;

    @InjectMocks
    AccountService service;

    private static final Long CUSTOMER_ID = 1L;
    private static final Customer CUSTOMER =
            new Customer(CUSTOMER_ID, "Test", "test@mail.com", "DOC001", LocalDateTime.now());

    private Account activeAccount(String number) {
        return new Account(10L, number, CUSTOMER_ID, AccountType.SAVINGS, "USD",
                Money.ZERO, AccountStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
    }

    // -------------------------------------------------------------------------
    // createAccount
    // -------------------------------------------------------------------------

    @Test
    void createAccount_customerExists_returnsSavedAccount() {
        given(customerOutputPort.findById(CUSTOMER_ID)).willReturn(Optional.of(CUSTOMER));
        given(accountOutputPort.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));

        Account result = service.createAccount(new CreateAccountCommand(CUSTOMER_ID, AccountType.SAVINGS));

        assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(result.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void createAccount_customerNotFound_throwsCustomerNotFoundException() {
        given(customerOutputPort.findById(CUSTOMER_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(CustomerNotFoundException.class)
                .isThrownBy(() -> service.createAccount(new CreateAccountCommand(CUSTOMER_ID, AccountType.SAVINGS)));
    }

    // -------------------------------------------------------------------------
    // getByAccountNumber
    // -------------------------------------------------------------------------

    @Test
    void getByAccountNumber_found_returnsAccount() {
        Account acc = activeAccount("NB0000000001");
        given(accountOutputPort.findByAccountNumber("NB0000000001")).willReturn(Optional.of(acc));

        Account result = service.getByAccountNumber("NB0000000001");

        assertThat(result.getAccountNumber()).isEqualTo("NB0000000001");
    }

    @Test
    void getByAccountNumber_notFound_throwsAccountNotFoundException() {
        given(accountOutputPort.findByAccountNumber("NB9999999999")).willReturn(Optional.empty());

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() -> service.getByAccountNumber("NB9999999999"));
    }

    // -------------------------------------------------------------------------
    // getByCustomerId
    // -------------------------------------------------------------------------

    @Test
    void getByCustomerId_returnsAllAccounts() {
        given(customerOutputPort.findById(CUSTOMER_ID)).willReturn(Optional.of(CUSTOMER));
        given(accountOutputPort.findByCustomerId(CUSTOMER_ID))
                .willReturn(List.of(activeAccount("NB0000000001"), activeAccount("NB0000000002")));

        List<Account> result = service.getByCustomerId(CUSTOMER_ID);

        assertThat(result).hasSize(2);
    }

    @Test
    void getByCustomerId_customerNotFound_throwsCustomerNotFoundException() {
        given(customerOutputPort.findById(CUSTOMER_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(CustomerNotFoundException.class)
                .isThrownBy(() -> service.getByCustomerId(CUSTOMER_ID));
    }

    // -------------------------------------------------------------------------
    // updateStatus
    // -------------------------------------------------------------------------

    @Test
    void updateStatus_toBlocked_changesStatusAndSaves() {
        Account account = activeAccount("0000000001");
        given(accountOutputPort.findByAccountNumber("0000000001")).willReturn(Optional.of(account));
        given(accountOutputPort.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));

        Account result = service.updateStatus(new UpdateAccountStatusCommand("0000000001", AccountStatus.BLOCKED));

        assertThat(result.getStatus()).isEqualTo(AccountStatus.BLOCKED);
    }

    @Test
    void updateStatus_toClosed_changesStatusAndSaves() {
        Account account = activeAccount("0000000001");
        given(accountOutputPort.findByAccountNumber("0000000001")).willReturn(Optional.of(account));
        given(accountOutputPort.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));

        Account result = service.updateStatus(new UpdateAccountStatusCommand("0000000001", AccountStatus.CLOSED));

        assertThat(result.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    void updateStatus_accountNotFound_throwsAccountNotFoundException() {
        given(accountOutputPort.findByAccountNumber("9999999999")).willReturn(Optional.empty());

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() -> service.updateStatus(
                        new UpdateAccountStatusCommand("9999999999", AccountStatus.BLOCKED)));
    }

    @Test
    void updateStatus_blockAlreadyClosedAccount_throwsInactiveAccountException() {
        Account account = activeAccount("0000000001");
        account.close();
        given(accountOutputPort.findByAccountNumber("0000000001")).willReturn(Optional.of(account));

        assertThatExceptionOfType(InactiveAccountException.class)
                .isThrownBy(() -> service.updateStatus(
                        new UpdateAccountStatusCommand("0000000001", AccountStatus.BLOCKED)));
    }
}
