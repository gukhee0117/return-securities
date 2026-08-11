package com.app.returns.domain.generalaccount.service;

import com.app.returns.domain.generalaccount.dto.GeneralAccountDTO;
import com.app.returns.domain.generalaccount.dto.request.GeneralAccountRequestDTO;
import com.app.returns.domain.generalaccount.dto.response.GeneralAccountResponseDTO;
import com.app.returns.domain.generalaccount.exception.GeneralAccountException;
import com.app.returns.domain.generalaccount.exception.GeneralAccountNotFoundException;
import com.app.returns.domain.generalaccount.mapper.GeneralAccountMapper;
import com.app.returns.domain.generalaccount.type.GeneralStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneralAccountServiceImplTest {

    @Mock
    private GeneralAccountMapper generalAccountMapper;

    @InjectMocks
    private GeneralAccountServiceImpl generalAccountService;

    @Test
    void verifyGeneralAccountThrowsNotFoundAndStopsWhenCustomerDoesNotExist() {
        GeneralAccountRequestDTO request = request();
        when(generalAccountMapper.existsCustomerByCiHash("ci-owner")).thenReturn(false);

        assertThatThrownBy(() -> generalAccountService.verifyGeneralAccount(request))
                .isInstanceOf(GeneralAccountNotFoundException.class)
                .hasMessage("고객을 찾을 수 없습니다.");

        verify(generalAccountMapper, never()).existsGeneralAccountById(request.getGeneralAccountId());
        verify(generalAccountMapper, never()).findByCiHashAndGeneralAccountId(request);
    }

    @Test
    void verifyGeneralAccountThrowsNotFoundAndStopsWhenAccountDoesNotExist() {
        GeneralAccountRequestDTO request = request();
        when(generalAccountMapper.existsCustomerByCiHash("ci-owner")).thenReturn(true);
        when(generalAccountMapper.existsGeneralAccountById(10L)).thenReturn(false);

        assertThatThrownBy(() -> generalAccountService.verifyGeneralAccount(request))
                .isInstanceOf(GeneralAccountNotFoundException.class)
                .hasMessage("계좌를 찾을 수 없습니다.");

        verify(generalAccountMapper, never()).findByCiHashAndGeneralAccountId(request);
    }

    @Test
    void verifyGeneralAccountRejectsAccountOwnedByAnotherCustomer() {
        GeneralAccountRequestDTO request = request();
        when(generalAccountMapper.existsCustomerByCiHash("ci-owner")).thenReturn(true);
        when(generalAccountMapper.existsGeneralAccountById(10L)).thenReturn(true);
        when(generalAccountMapper.findByCiHashAndGeneralAccountId(request)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generalAccountService.verifyGeneralAccount(request))
                .isExactlyInstanceOf(GeneralAccountException.class)
                .hasMessage("해당 고객 소유의 계좌가 아닙니다.");
    }

    @Test
    void verifyGeneralAccountRejectsClosedAccount() {
        GeneralAccountRequestDTO request = request();
        stubExistingAccount(request, GeneralStatus.CLOSED);

        assertThatThrownBy(() -> generalAccountService.verifyGeneralAccount(request))
                .isExactlyInstanceOf(GeneralAccountException.class)
                .hasMessage("해지된 일반계좌입니다.");
    }

    @Test
    void verifyGeneralAccountReturnsOnlyVerifiedActiveAccountFields() {
        GeneralAccountRequestDTO request = request();
        stubExistingAccount(request, GeneralStatus.ACTIVE);

        GeneralAccountResponseDTO result = generalAccountService.verifyGeneralAccount(request);

        assertThat(result.getGeneralAccountId()).isEqualTo(10L);
        assertThat(result.getAccountNo()).isEqualTo("1000000001");
        assertThat(result.getStatus()).isEqualTo(GeneralStatus.ACTIVE);
    }

    private GeneralAccountRequestDTO request() {
        return GeneralAccountRequestDTO.builder()
                .ciHash("ci-owner")
                .generalAccountId(10L)
                .build();
    }

    private void stubExistingAccount(GeneralAccountRequestDTO request, GeneralStatus status) {
        GeneralAccountDTO account = GeneralAccountDTO.builder()
                .generalAccountId(10L)
                .generalCustomerId(1L)
                .accountNo("1000000001")
                .accountType("BROKERAGE")
                .status(status)
                .build();
        when(generalAccountMapper.existsCustomerByCiHash("ci-owner")).thenReturn(true);
        when(generalAccountMapper.existsGeneralAccountById(10L)).thenReturn(true);
        when(generalAccountMapper.findByCiHashAndGeneralAccountId(request)).thenReturn(Optional.of(account));
    }
}
