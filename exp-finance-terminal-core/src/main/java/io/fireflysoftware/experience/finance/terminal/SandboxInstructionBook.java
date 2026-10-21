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

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Sample mandate, standing order, and direct debit. Nothing is opened on the network.
 */
public final class SandboxInstructionBook implements InstructionPort {

    static final UUID SCHEDULE = UUID.fromString("10000000-0000-4000-8000-0000000000b1");

    @Override
    public Mono<Mandate> mandate(UUID accountId, UUID mandateId) {
        return Mono.just(new Mandate(mandateId, accountId, FinanceSamples.CUSTOMER_PARTY, "OPERATOR", true));
    }

    @Override
    public Mono<StandingOrder> standingOrder(UUID runId) {
        return Mono.just(new StandingOrder(runId, SCHEDULE, new BigDecimal("25.00"), "GBP", LocalDate.of(2026, 10, 21)));
    }

    @Override
    public Mono<DirectDebit> directDebit(UUID mandateId) {
        return Mono.just(new DirectDebit(mandateId, FinanceArea.ACCOUNT.primaryId(), FinanceSamples.CUSTOMER_PARTY,
                "ACTIVE", new BigDecimal("100.00"), "GBP", "MANDATE-1"));
    }
}
