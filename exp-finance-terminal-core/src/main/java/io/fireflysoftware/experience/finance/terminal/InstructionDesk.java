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

import java.util.UUID;

/**
 * An employee reads mandates, standing orders, and direct debits.
 * Administer is required. The read does not impersonate, and a denial does not call the port.
 */
public final class InstructionDesk {

    private final IdentityPort identity;
    private final AccessPort access;
    private final InstructionPort instructions;
    private final String anchorType;
    private final String anchorResourceId;

    public InstructionDesk(IdentityPort identity, AccessPort access, InstructionPort instructions,
                           String anchorType, String anchorResourceId) {
        this.identity = identity;
        this.access = access;
        this.instructions = instructions;
        this.anchorType = anchorType;
        this.anchorResourceId = anchorResourceId;
    }

    public Mono<InstructionPort.Mandate> mandate(UUID actorId, UUID accountId, UUID mandateId) {
        return require(actorId).then(Mono.defer(() -> instructions.mandate(accountId, mandateId)));
    }

    public Mono<InstructionPort.StandingOrder> standingOrder(UUID actorId, UUID runId) {
        return require(actorId).then(Mono.defer(() -> instructions.standingOrder(runId)));
    }

    public Mono<InstructionPort.DirectDebit> directDebit(UUID actorId, UUID mandateId) {
        return require(actorId).then(Mono.defer(() -> instructions.directDebit(mandateId)));
    }

    private Mono<Void> require(UUID actorId) {
        if (actorId == null) {
            return Mono.error(new TerminalDeniedException("REQUEST_INCOMPLETE"));
        }
        return identity.resolve(actorId.toString())
                .switchIfEmpty(Mono.error(new TerminalDeniedException("ACTOR_UNKNOWN")))
                .flatMap(subject -> access.check(new AccessCheck(subject.actorId(), "ADMINISTER", anchorType, anchorResourceId)))
                .flatMap(outcome -> outcome.allowed()
                        ? Mono.empty()
                        : Mono.error(new TerminalDeniedException(outcome.reasonCode())));
    }
}
