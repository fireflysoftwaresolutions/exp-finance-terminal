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

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstructionDeskTest {

    private static final UUID EMPLOYEE = UUID.fromString("33333333-3333-4333-8333-333333333333");
    private static final UUID CUSTOMER = UUID.fromString("11111111-1111-4111-8111-111111111111");

    @Test
    void employeeReadsTheSampleInstructionsWithoutASocket() {
        InstructionDesk desk = desk(true, new SandboxInstructionBook());
        UUID accountId = FinanceArea.ACCOUNT.primaryId();
        UUID mandateId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();

        InstructionPort.Mandate mandate = desk.mandate(EMPLOYEE, accountId, mandateId).block();
        InstructionPort.StandingOrder order = desk.standingOrder(EMPLOYEE, runId).block();
        InstructionPort.DirectDebit debit = desk.directDebit(EMPLOYEE, mandateId).block();

        assertEquals("OPERATOR", mandate.role());
        assertEquals(FinanceSamples.CUSTOMER_PARTY, mandate.partyId());
        assertEquals("25.00", order.amount().toPlainString());
        assertEquals("GBP", order.currency());
        assertEquals("MANDATE-1", debit.reference());
        assertEquals("100.00", debit.amountLimit().toPlainString());
    }

    @Test
    void deniedAdministerDoesNotRead() {
        AtomicInteger reads = new AtomicInteger();
        InstructionPort counting = new InstructionPort() {
            @Override
            public Mono<Mandate> mandate(UUID accountId, UUID mandateId) {
                reads.incrementAndGet();
                return Mono.empty();
            }

            @Override
            public Mono<StandingOrder> standingOrder(UUID runId) {
                reads.incrementAndGet();
                return Mono.empty();
            }

            @Override
            public Mono<DirectDebit> directDebit(UUID mandateId) {
                reads.incrementAndGet();
                return Mono.empty();
            }
        };
        InstructionDesk desk = desk(false, counting);

        TerminalDeniedException error = assertThrows(TerminalDeniedException.class,
                () -> desk.mandate(CUSTOMER, FinanceArea.ACCOUNT.primaryId(), UUID.randomUUID()).block());
        assertEquals("ACTION_NOT_GRANTED", error.reasonCode());
        assertEquals(0, reads.get());
    }

    private static InstructionDesk desk(boolean allow, InstructionPort instructions) {
        IdentityPort identity = new IdentityPort() {
            @Override
            public Mono<SubjectRef> authenticate(String username) {
                return Mono.empty();
            }

            @Override
            public Mono<SubjectRef> resolve(String actorId) {
                return Mono.just(new SubjectRef(UUID.fromString(actorId), "sandbox"));
            }
        };
        AccessPort access = request -> Mono.just(allow && "ADMINISTER".equals(request.action())
                ? new AccessOutcome(true, "SANDBOX_ALLOW")
                : new AccessOutcome(false, "ACTION_NOT_GRANTED"));
        return new InstructionDesk(identity, access, instructions, "ACCOUNT", FinanceArea.ACCOUNT.primaryId().toString());
    }
}
