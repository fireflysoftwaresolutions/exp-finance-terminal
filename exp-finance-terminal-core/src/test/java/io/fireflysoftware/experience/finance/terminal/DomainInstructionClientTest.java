/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.finance.terminal;

import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountRecordsControllerApi;
import io.fireflysoftware.domain.banking.accounts.sdk.model.MandateView;
import io.fireflysoftware.domain.payments.sdk.api.PaymentRecordsApi;
import io.fireflysoftware.domain.payments.sdk.model.DirectDebitView;
import io.fireflysoftware.domain.payments.sdk.model.StandingOrderView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainInstructionClientTest {

    @Mock
    private AccountRecordsControllerApi accounts;
    @Mock
    private PaymentRecordsApi payments;

    @Test
    void readsGoThroughTheDomainSdks() {
        UUID accountId = UUID.randomUUID();
        UUID mandateId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID creditorId = UUID.randomUUID();
        when(accounts.servicingMandate1(accountId, mandateId, null)).thenReturn(Mono.just(
                new MandateView().id(mandateId).accountId(accountId).partyId(partyId).role("OPERATOR").active(true)));
        when(payments.readStandingOrder(runId)).thenReturn(Mono.just(
                new StandingOrderView().id(runId).paymentScheduleId(scheduleId).amount(new BigDecimal("25.00"))
                        .currency("GBP").valueDate(LocalDate.of(2026, 10, 21))));
        when(payments.readDirectDebit(mandateId)).thenReturn(Mono.just(
                new DirectDebitView().id(mandateId).debtorAccountId(accountId).creditorPartyId(creditorId)
                        .status("ACTIVE").amountLimit(new BigDecimal("100.00")).currency("GBP").reference("MANDATE-1")));

        DomainInstructionClient client = new DomainInstructionClient(accounts, payments);
        InstructionPort.Mandate mandate = client.mandate(accountId, mandateId).block();
        InstructionPort.StandingOrder order = client.standingOrder(runId).block();
        InstructionPort.DirectDebit debit = client.directDebit(mandateId).block();

        assertThat(mandate.role()).isEqualTo("OPERATOR");
        assertThat(order.amount()).isEqualByComparingTo("25.00");
        assertThat(debit.reference()).isEqualTo("MANDATE-1");
        verify(accounts).servicingMandate1(accountId, mandateId, null);
        verify(payments).readStandingOrder(runId);
        verify(payments).readDirectDebit(mandateId);
    }
}
