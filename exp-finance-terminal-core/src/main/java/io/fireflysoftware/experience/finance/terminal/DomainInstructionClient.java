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
import io.fireflysoftware.domain.payments.sdk.api.PaymentRecordsApi;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reads the three instructions through the domain SDKs. Those domains read the cores.
 */
public final class DomainInstructionClient implements InstructionPort {

    private final AccountRecordsControllerApi accounts;
    private final PaymentRecordsApi payments;

    public DomainInstructionClient(AccountRecordsControllerApi accounts, PaymentRecordsApi payments) {
        this.accounts = accounts;
        this.payments = payments;
    }

    @Override
    public Mono<Mandate> mandate(UUID accountId, UUID mandateId) {
        return accounts.servicingMandate1(accountId, mandateId, null)
                .map(row -> new Mandate(row.getId(), row.getAccountId(), row.getPartyId(), row.getRole(),
                        Boolean.TRUE.equals(row.getActive())));
    }

    @Override
    public Mono<StandingOrder> standingOrder(UUID runId) {
        return payments.readStandingOrder(runId)
                .map(row -> new StandingOrder(row.getId(), row.getPaymentScheduleId(), row.getAmount(),
                        row.getCurrency(), row.getValueDate()));
    }

    @Override
    public Mono<DirectDebit> directDebit(UUID mandateId) {
        return payments.readDirectDebit(mandateId)
                .map(row -> new DirectDebit(row.getId(), row.getDebtorAccountId(), row.getCreditorPartyId(),
                        row.getStatus(), row.getAmountLimit(), row.getCurrency(), row.getReference()));
    }
}
