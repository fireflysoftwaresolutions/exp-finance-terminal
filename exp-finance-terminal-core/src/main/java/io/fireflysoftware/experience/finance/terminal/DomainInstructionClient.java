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
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reads the three instructions through the domain SDKs. Those domains read the cores.
 */
public final class DomainInstructionClient implements InstructionPort {

    private final AccountRecordsApi accounts;
    private final PaymentRecordsApi payments;

    public DomainInstructionClient(AccountRecordsApi accounts, PaymentRecordsApi payments) {
        this.accounts = accounts;
        this.payments = payments;
    }

    @Override
    public Mono<Mandate> mandate(UUID accountId, UUID mandateId) {
        return accounts.servicingMandate(accountId, mandateId)
                .map(row -> new Mandate(row.id(), row.accountId(), row.partyId(), row.role(), row.active()));
    }

    @Override
    public Mono<StandingOrder> standingOrder(UUID runId) {
        return payments.standingOrder(runId)
                .map(row -> new StandingOrder(row.id(), row.paymentScheduleId(), row.amount(), row.currency(), row.valueDate()));
    }

    @Override
    public Mono<DirectDebit> directDebit(UUID mandateId) {
        return payments.directDebit(mandateId)
                .map(row -> new DirectDebit(row.id(), row.debtorAccountId(), row.creditorPartyId(), row.status(),
                        row.amountLimit(), row.currency(), row.reference()));
    }
}
