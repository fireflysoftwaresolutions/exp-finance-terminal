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
 * Reads a servicing mandate, a standing-order run, and a direct-debit mandate.
 * The domain adapter calls the accounts and payments domain SDKs. The sandbox does not open a socket.
 */
public interface InstructionPort {

    Mono<Mandate> mandate(UUID accountId, UUID mandateId);

    Mono<StandingOrder> standingOrder(UUID runId);

    Mono<DirectDebit> directDebit(UUID mandateId);

    record Mandate(UUID id, UUID accountId, UUID partyId, String role, boolean active) {
    }

    record StandingOrder(UUID id, UUID paymentScheduleId, BigDecimal amount, String currency, LocalDate valueDate) {
    }

    record DirectDebit(UUID id, UUID debtorAccountId, UUID creditorPartyId, String status,
                        BigDecimal amountLimit, String currency, String reference) {
    }
}
