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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffTerminalTest {

    private static final UUID CUSTOMER = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID DENIED = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID EMPLOYEE = UUID.fromString("33333333-3333-4333-8333-333333333333");

    private SecurityCenterSandbox security;
    private StaffTerminal terminal;

    @BeforeEach
    void setUp() throws Exception {
        security = new SecurityCenterSandbox();
        terminal = TerminalFixture.finance(security.baseUrl);
    }

    @AfterEach
    void tearDown() {
        security.close();
    }

    @Test
    void sampleEmployeeSignsIn() {
        StaffSession session = terminal.signIn("sample-employee").block();
        assertEquals(EMPLOYEE, session.actorId());
        assertEquals("sandbox", session.provider());
    }

    @Test
    void customerSubjectCannotOpenTheTerminal() {
        TerminalDeniedException error = assertThrows(TerminalDeniedException.class,
                () -> terminal.signIn("sample-journey").block());
        assertEquals("ACTION_NOT_GRANTED", error.reasonCode());
    }

    @Test
    void deniedActorCannotOpenTheTerminal() {
        TerminalDeniedException error = assertThrows(TerminalDeniedException.class,
                () -> terminal.signIn("denied-actor").block());
        assertEquals("ACTOR_NOT_ENTITLED", error.reasonCode());
    }

    @Test
    void unknownUsernameIsRejected() {
        TerminalDeniedException error = assertThrows(TerminalDeniedException.class,
                () -> terminal.signIn("nobody").block());
        assertEquals("ACTOR_UNKNOWN", error.reasonCode());
    }

    @Test
    void customerCanReadTheSampleAccountAndStillCannotAdminister() {
        WebClientAccess access = new WebClientAccess(security.baseUrl);
        AccessOutcome read = access.check(new AccessCheck(CUSTOMER, "READ", "ACCOUNT",
                FinanceArea.ACCOUNT.primaryId().toString())).block();
        assertTrue(read.allowed());
        TerminalDeniedException error = assertThrows(TerminalDeniedException.class,
                () -> terminal.open(CUSTOMER, FinanceArea.ACCOUNT, FinanceArea.ACCOUNT.primaryId()).block());
        assertEquals("ACTION_NOT_GRANTED", error.reasonCode());
    }

    @Test
    void sampleEmployeeImpersonatesBeforeACustomerUpdate() {
        RecordCommand command = new RecordCommand(FinanceArea.ACCOUNT, FinanceArea.ACCOUNT.primaryId(),
                FinanceSamples.CUSTOMER_PARTY, "Customer asked for a note", "Reviewed by staff",
                null, null, null, null, null, null);
        StaffRecord updated = terminal.update(EMPLOYEE, command).block();
        assertEquals(EMPLOYEE, updated.actorId());
        assertEquals(FinanceSamples.CUSTOMER_PARTY, updated.customerPartyId());
        assertEquals("Reviewed by staff", updated.note());
        AuditEntry audit = terminal.audit(EMPLOYEE).blockFirst();
        assertEquals(EMPLOYEE, audit.employeeId());
        assertEquals(FinanceSamples.CUSTOMER_PARTY, audit.customerPartyId());
        assertEquals("Customer asked for a note", audit.reason());
        assertTrue(security.accessBodies().stream().anyMatch(body -> body.contains("IMPERSONATE")
                && body.contains(EMPLOYEE.toString())
                && body.contains(FinanceSamples.CUSTOMER_PARTY.toString())));
    }

    @Test
    void deniedActorCannotImpersonateAndTheRecordStaysUnchanged() {
        RecordCommand command = new RecordCommand(FinanceArea.ACCOUNT, FinanceArea.ACCOUNT.primaryId(),
                FinanceSamples.CUSTOMER_PARTY, "Should not apply", "Changed",
                null, null, null, null, null, null);
        TerminalDeniedException error = assertThrows(TerminalDeniedException.class,
                () -> terminal.update(DENIED, command).block());
        assertEquals("ACTOR_NOT_ENTITLED", error.reasonCode());
        StaffRecord record = terminal.open(EMPLOYEE, FinanceArea.ACCOUNT, FinanceArea.ACCOUNT.primaryId()).block();
        assertEquals(null, record.note());
        assertTrue(security.accessBodies().stream().noneMatch(body -> body.contains("IMPERSONATE")));
    }

    @Test
    void customerCannotImpersonate() {
        RecordCommand command = RecordCommand.of(FinanceArea.CARD, FinanceArea.CARD.primaryId(),
                FinanceSamples.CUSTOMER_PARTY, "Customer acting as staff");
        TerminalDeniedException error = assertThrows(TerminalDeniedException.class,
                () -> terminal.update(CUSTOMER, command).block());
        assertEquals("ACTION_NOT_GRANTED", error.reasonCode());
    }

    @Test
    void collateralCloseUsesReleasedFromConfiguration() {
        RecordCommand command = RecordCommand.of(FinanceArea.COLLATERAL_CASE, FinanceArea.COLLATERAL_CASE.primaryId(),
                FinanceSamples.CUSTOMER_PARTY, "Release the lien");
        StaffRecord closed = terminal.close(EMPLOYEE, command).block();
        assertEquals("RELEASED", closed.status());
        assertEquals(EMPLOYEE, closed.actorId());
    }

    @Test
    void provisioningCloseUsesReleasedFromConfiguration() {
        RecordCommand command = RecordCommand.of(FinanceArea.PROVISIONING_CASE, FinanceArea.PROVISIONING_CASE.primaryId(),
                FinanceSamples.CUSTOMER_PARTY, "Release the provision");
        StaffRecord closed = terminal.close(EMPLOYEE, command).block();
        assertEquals("RELEASED", closed.status());
    }

    @Test
    void supplyChainDraftCanBeRemovedAndAnActiveAgreementCannot() {
        RecordCommand draft = RecordCommand.of(FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT,
                FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT.primaryId(), FinanceSamples.CUSTOMER_PARTY, "Remove the draft");
        terminal.removeDraft(EMPLOYEE, draft).block();
        TerminalDeniedException missing = assertThrows(TerminalDeniedException.class,
                () -> terminal.open(EMPLOYEE, FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT,
                        FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT.primaryId()).block());
        assertEquals("RECORD_NOT_FOUND", missing.reasonCode());

        RecordCommand active = RecordCommand.of(FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT, FinanceSamples.SCF_ACTIVE,
                FinanceSamples.CUSTOMER_PARTY, "Keep the active agreement");
        TerminalDeniedException denied = assertThrows(TerminalDeniedException.class,
                () -> terminal.removeDraft(EMPLOYEE, active).block());
        assertEquals("DRAFT_REQUIRED", denied.reasonCode());
        StaffRecord stillThere = terminal.open(EMPLOYEE, FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT, FinanceSamples.SCF_ACTIVE).block();
        assertEquals("ACTIVE", stillThere.status());
    }

    @Test
    void employeeAttachesADistributorUuid() {
        RecordCommand command = new RecordCommand(FinanceArea.LENDING_APPLICATION, FinanceArea.LENDING_APPLICATION.primaryId(),
                FinanceSamples.CUSTOMER_PARTY, "Attach the broker", null, FinanceSamples.BROKER,
                UUID.fromString("b3000000-0000-4000-8000-000000000001"), null, null, null, null);
        StaffRecord updated = terminal.attachDistributor(EMPLOYEE, command).block();
        assertEquals(FinanceSamples.BROKER, updated.distributorId());
        assertEquals(EMPLOYEE, updated.actorId());
    }

    @Test
    void propertyChangeGoesThroughConfigurationAndRequiresAdminister() {
        String key = "finance-terminal.distributor.channel";
        String updated = terminal.putProperty(EMPLOYEE, key, "PARTNER").block();
        assertEquals("PARTNER", updated);
        assertEquals("PARTNER", terminal.getProperty(EMPLOYEE, key).block());
        TerminalDeniedException error = assertThrows(TerminalDeniedException.class,
                () -> terminal.putProperty(CUSTOMER, key, "WEB").block());
        assertEquals("ACTION_NOT_GRANTED", error.reasonCode());
        assertEquals("PARTNER", terminal.getProperty(EMPLOYEE, key).block());
    }

    @Test
    void employeeListsSampleAccounts() {
        StaffRecord account = terminal.list(EMPLOYEE, FinanceArea.ACCOUNT).blockFirst();
        assertEquals(FinanceArea.ACCOUNT.primaryId(), account.id());
    }

    private static final class WebClientAccess implements AccessPort {
        private final AccessPort delegate;

        private WebClientAccess(String baseUrl) {
            delegate = HttpAccessAdapter.at(baseUrl);
        }

        @Override
        public reactor.core.publisher.Mono<AccessOutcome> check(AccessCheck request) {
            return delegate.check(request);
        }
    }
}
