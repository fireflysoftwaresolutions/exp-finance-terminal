/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

/** Actual generated HTTP transport, not current STAFF authorization or persisted owner acceptance. */
class DomainRecordOutcomeAdditionalContractTest {
    private static final UUID ID=UUID.fromString("10000000-0000-4000-8000-000000000004");
    private static final Duration TIMEOUT=Duration.ofSeconds(3);
    private static RecordCommand command(FinanceArea area,String decision){return new RecordCommand(area,ID,FinanceSamples.CUSTOMER_PARTY,
        "Caller reason","Caller note",null,null,null,UUID.fromString("20000000-0000-4000-8000-000000000001"),"CLOSED",decision);}

    @Test void unavailableMutationRetainsTheSameOriginalRequestAndActuallySentKeyWithoutRetry()throws Exception{
        try(var fixture=new DomainRecordOutcomeContractTest.Fixture(request->new DomainRecordOutcomeContractTest.Reply(503,"{}"))){
            var original=command(FinanceArea.COLLATERAL_CASE,null);
            var unknown=assertThrows(RecordOutcomeUnknownException.class,()->fixture.client.close(original).block(TIMEOUT));
            assertSame(original,unknown.originalRequest());assertNotNull(unknown.transportRequestKey());
            assertEquals(fixture.requestKey.get(),unknown.transportRequestKey());assertEquals(1,fixture.calls.get());
        }
    }

    @Test void currentOwnerConflictRemainsARefusalAndNeverAConfirmedRecord()throws Exception{
        try(var fixture=new DomainRecordOutcomeContractTest.Fixture(request->new DomainRecordOutcomeContractTest.Reply(409,"{}"))){
            var refused=assertThrows(ResponseStatusException.class,()->fixture.client.close(command(FinanceArea.COLLATERAL_CASE,null)).block(TIMEOUT));
            assertEquals(409,refused.getStatusCode().value());assertEquals(1,fixture.calls.get());
        }
    }

    @Test void readRefusalAfterSuccessfulCloseKeepsTheOriginalWriteUnknownInsteadOfRefused()throws Exception{
        try(var fixture=new DomainRecordOutcomeContractTest.Fixture(request->new DomainRecordOutcomeContractTest.Reply(
                request.getRequestMethod().equals("GET")?403:200,""))){
            var original=command(FinanceArea.ACCOUNT,null);
            var unknown=assertThrows(RecordOutcomeUnknownException.class,()->fixture.client.close(original).block(TIMEOUT));
            assertSame(original,unknown.originalRequest());assertEquals(fixture.writeKey.get(),unknown.transportRequestKey());
            assertEquals(2,fixture.calls.get());assertEquals("GET",fixture.verb.get());
        }
    }

    @Test void activeConfiguredRemovalRequirementCannotAuthorizeAnyDeleteOrRead()throws Exception{
        try(var fixture=new DomainRecordOutcomeContractTest.Fixture(request->new DomainRecordOutcomeContractTest.Reply(200,"{}"))){
            var denied=assertThrows(TerminalDeniedException.class,()->fixture.client.removeDraft(command(FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT,null),"ACTIVE").block(TIMEOUT));
            assertEquals("DRAFT_REQUIRED",denied.reasonCode());assertEquals(0,fixture.calls.get());
        }
    }

    @Test void missingMaskedReferenceNeverExposesTheReturnedFullCardNumber()throws Exception{
        try(var fixture=new DomainRecordOutcomeContractTest.Fixture(request->new DomainRecordOutcomeContractTest.Reply(200,
                "{\"cardId\":\""+ID+"\",\"cardStatus\":\"ACTIVE\",\"cardNumber\":\"4111111111111111\"}"))){
            var returned=fixture.client.get(FinanceArea.CARD,ID).block(TIMEOUT);
            assertNotNull(returned);assertEquals("ACTIVE",returned.status());assertNull(returned.reference());assertEquals(1,fixture.calls.get());
        }
    }

    @Test void unknownLoanDecisionCannotDispatchAsARejection()throws Exception{
        try(var fixture=new DomainRecordOutcomeContractTest.Fixture(request->new DomainRecordOutcomeContractTest.Reply(200,"{}"))){
            var denied=assertThrows(TerminalDeniedException.class,()->fixture.client.decide(command(FinanceArea.LENDING_APPLICATION,"TYPO")).block(TIMEOUT));
            assertEquals("DECISION_NOT_APPLICABLE",denied.reasonCode());assertEquals(0,fixture.calls.get());
        }
    }

    @Test void legitimateLoanReplyReturnsTheRealFollowUpStatusIdentityWithoutInventingAName()throws Exception{
        String statusId="30000000-0000-4000-8000-000000000001";
        try(var fixture=new DomainRecordOutcomeContractTest.Fixture(request->new DomainRecordOutcomeContractTest.Reply(200,
                request.getRequestMethod().equals("GET")?"{\"loanApplicationId\":\""+ID+"\",\"applicationStatusId\":\""+statusId+"\",\"note\":\"OWNER-NOTE\"}":"{\"completed\":true}"))){
            var returned=fixture.client.decide(command(FinanceArea.LENDING_APPLICATION,"APPROVE")).block(TIMEOUT);
            assertNotNull(returned);assertEquals(ID,returned.id());assertEquals(statusId,returned.status());
            assertNotEquals("APPROVED",returned.status());assertNotEquals("CLOSED",returned.status());
            assertEquals("OWNER-NOTE",returned.note());assertNull(returned.actorId());assertNull(returned.customerPartyId());
            assertEquals(2,fixture.calls.get());assertEquals("GET",fixture.verb.get());
        }
    }
}
