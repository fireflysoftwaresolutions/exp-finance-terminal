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

import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountRecordsApi;
import io.fireflysoftware.domain.payments.sdk.api.PaymentRecordsApi;
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
    private AccountRecordsApi accounts;
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
        when(accounts.servicingMandate(accountId, mandateId)).thenReturn(Mono.just(
                new AccountRecordsApi.MandateView(mandateId, accountId, partyId, "OPERATOR", true)));
        when(payments.standingOrder(runId)).thenReturn(Mono.just(
                new PaymentRecordsApi.StandingOrderView(runId, scheduleId, new BigDecimal("25.00"), "GBP",
                        LocalDate.of(2026, 10, 21))));
        when(payments.directDebit(mandateId)).thenReturn(Mono.just(
                new PaymentRecordsApi.DirectDebitView(mandateId, accountId, creditorId, "ACTIVE",
                        new BigDecimal("100.00"), "GBP", "MANDATE-1")));

        DomainInstructionClient client = new DomainInstructionClient(accounts, payments);
        InstructionPort.Mandate mandate = client.mandate(accountId, mandateId).block();
        InstructionPort.StandingOrder order = client.standingOrder(runId).block();
        InstructionPort.DirectDebit debit = client.directDebit(mandateId).block();

        assertThat(mandate.role()).isEqualTo("OPERATOR");
        assertThat(order.amount()).isEqualByComparingTo("25.00");
        assertThat(debit.reference()).isEqualTo("MANDATE-1");
        verify(accounts).servicingMandate(accountId, mandateId);
        verify(payments).standingOrder(runId);
        verify(payments).directDebit(mandateId);
    }
}
