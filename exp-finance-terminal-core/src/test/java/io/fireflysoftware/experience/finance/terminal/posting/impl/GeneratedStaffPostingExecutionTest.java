/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.posting.impl;
import static org.junit.jupiter.api.Assertions.*;
import io.fireflysoftware.experience.finance.terminal.posting.*;
import io.fireflysoftware.domain.accounting.sdk.api.ManagedLedgerJournalsApi;
import io.fireflysoftware.domain.accounting.sdk.invoker.ApiClient;
import java.time.*;
import java.util.UUID;
import java.util.concurrent.atomic.*;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.http.codec.json.*;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
/** Actual original generated Domain transport, controlled responses only; not financial owner acceptance. */
class GeneratedStaffPostingExecutionTest {
    static final String TENANT=new UUID(0,1).toString(),BOOK=new UUID(0,2).toString(),ACTION=new UUID(0,3).toString(),DIGEST="a".repeat(64);
    final AtomicInteger calls=new AtomicInteger(),rechecks=new AtomicInteger();final AtomicReference<ClientRequest> request=new AtomicReference<>();final AtomicBoolean current=new AtomicBoolean(true);
    Function<ClientRequest,Mono<ClientResponse>> wire=input->Mono.just(response(200,committed(false)));
    StaffPostingInvocation original(boolean recovery){return new StaffPostingInvocation(TENANT,BOOK,UUID.fromString(ACTION),DIGEST,"{\"expectedIntentDigest\":\""+DIGEST+"\",\"posting\":{\"actionId\":\""+ACTION+"\"}}",recovery);}
    String committed(boolean recovered){return "{\"actionId\":\""+ACTION+"\",\"intentDigest\":\""+DIGEST+"\",\"status\":\"COMMITTED\",\"ownerStatus\":200,\"receipt\":{\"receiptId\":\""+new UUID(0,4)+"\",\"actionId\":\""+ACTION+"\",\"postingId\":\""+new UUID(0,5)+"\",\"journalSequence\":1,\"intentDigest\":\""+DIGEST+"\",\"recovered\":"+recovered+",\"recordedAt\":\"2026-11-01T12:00:00Z\"}}";}
    String outcome(String status,Integer owner){return "{\"actionId\":\""+ACTION+"\",\"intentDigest\":\""+DIGEST+"\",\"status\":\""+status+"\",\"ownerStatus\":"+owner+",\"receipt\":null}";}
    ExchangeStrategies strategies(){var mapper=ApiClient.createDefaultMapper(ApiClient.createDefaultDateFormat());return ExchangeStrategies.builder().codecs(c->{c.customCodecs().register(new Jackson2JsonDecoder(mapper));c.customCodecs().register(new Jackson2JsonEncoder(mapper));}).build();}
    ClientResponse response(int status,String body){return ClientResponse.create(HttpStatusCode.valueOf(status),strategies()).header("Content-Type","application/json").body(body).build();}
    StaffPostingExecution execution(){return new GeneratedStaffPostingExecutionImpl((tenant,book,recovery)->{
        assertEquals(TENANT,tenant);assertEquals(BOOK,book);var mapper=ApiClient.createDefaultMapper(ApiClient.createDefaultDateFormat());var transport=ApiClient.buildWebClientBuilder(mapper).exchangeFunction(actual->{calls.incrementAndGet();request.set(actual);return wire.apply(actual);}).build();
        var client=new ApiClient(transport,mapper,ApiClient.createDefaultDateFormat()).setBasePath("https://accounting-domain.example");client.addDefaultHeader("Authorization","Bearer controlled-domain-token");
        return Mono.just(new StaffPostingClientFactory.Client(){public ManagedLedgerJournalsApi api(){return new ManagedLedgerJournalsApi(client);}public Duration remaining(){return Duration.ofSeconds(5);}public Mono<Void> revalidate(){return Mono.defer(()->{rechecks.incrementAndGet();return current.get()?Mono.empty():Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN));});}});
    });}
    @Test void originalGeneratedDomainReceiptUsesActualInstantAbi()throws Exception{assertEquals(Instant.class,io.fireflysoftware.domain.accounting.sdk.model.ManagedPostingReceipt.class.getMethod("getRecordedAt").getReturnType());}
    @Test void oneGeneratedDomainPostProjectsOnlyTheBoundReceipt(){var value=execution().execute(original(false)).block();assertEquals(StaffPostingResponse.Status.COMMITTED,value.status());assertEquals(200,value.domainStatus());assertEquals(200,value.ownerStatus());assertEquals(UUID.fromString(ACTION),value.receipt().actionId());assertEquals(Instant.parse("2026-11-01T12:00:00Z"),value.receipt().recordedAt());assertEquals(1,calls.get());assertEquals("/api/v1/tenants/"+TENANT+"/books/"+BOOK+"/ledger-journals",request.get().url().getPath());assertFalse(request.get().url().getPath().contains("/postings"));assertFalse(request.get().headers().containsHeader("X-Idempotency-Key"));assertTrue(rechecks.get()>=2);}
    @Test void recoveryUsesTheSameOriginalActionAndOnlyTheDomainRecoveryRoute(){wire=input->Mono.just(response(200,committed(true)));var value=execution().execute(original(true)).block();assertEquals(StaffPostingResponse.Status.COMMITTED,value.status());assertTrue(value.receipt().recovered());assertEquals(1,calls.get());assertTrue(request.get().url().getPath().endsWith("/ledger-journals/recovery"));}
    @Test void exhaustedNativeCallerCannotDispatchDomain(){current.set(false);assertThrows(StaffPostingPreDispatchException.class,()->execution().execute(original(false)).block());assertEquals(0,calls.get());}
    @Test void lostDomainResponseNeverCreatesAnotherActionOrRetries(){wire=input->Mono.error(new java.io.IOException("controlled lost delivery"));var value=execution().execute(original(false)).block();assertEquals(StaffPostingResponse.Status.OUTCOME_UNKNOWN,value.status());assertEquals(UUID.fromString(ACTION),value.actionId());assertEquals(DIGEST,value.intentDigest());assertEquals(1,calls.get());}
    @Test void unavailableUnboundDomainCannotClaimNoEffects(){wire=input->Mono.just(response(503,"{}"));assertEquals(StaffPostingResponse.Status.OUTCOME_UNKNOWN,execution().execute(original(false)).block().status());assertEquals(1,calls.get());}
    @Test void correlatedKnownCommitUnavailableRemainsKnownWithoutLeakingReceipt(){wire=input->Mono.just(response(503,outcome("COMMITTED_RESPONSE_UNAVAILABLE",503)));var value=execution().execute(original(false)).block();assertEquals(StaffPostingResponse.Status.COMMITTED_RESPONSE_UNAVAILABLE,value.status());assertEquals(503,value.domainStatus());assertNull(value.receipt());}
    @Test void unrelatedOriginalActionCannotBecomeCommitted(){wire=input->Mono.just(response(200,committed(false).replace(ACTION,new UUID(0,99).toString())));assertEquals(StaffPostingResponse.Status.OUTCOME_UNKNOWN,execution().execute(original(false)).block().status());}
    @Test void correlatedOwnerRefusalPreservesBothDomainAndCoreStatus(){wire=input->Mono.just(response(409,outcome("CURRENT_INVOCATION_REFUSED",409)));var value=execution().execute(original(false)).block();assertEquals(StaffPostingResponse.Status.CURRENT_INVOCATION_REFUSED,value.status());assertEquals(409,value.domainStatus());assertEquals(409,value.ownerStatus());}
    @Test void domainPreDispatchEnvelopeIsNotConfusedWithExperiencePreDispatchOrCoreRefusal(){wire=input->Mono.just(response(403,"{\"status\":403,\"code\":\"MANAGED_POSTING_PRE_DISPATCH_REJECTED\",\"phase\":\"PRE_DISPATCH\"}"));var value=execution().execute(original(false)).block();assertEquals(StaffPostingResponse.Status.DOMAIN_PRE_DISPATCH_REFUSED,value.status());assertEquals(403,value.domainStatus());assertNull(value.ownerStatus());assertEquals(UUID.fromString(ACTION),value.actionId());}
    @Test void nativeDomainAuthenticationRefusalDoesNotInventACoreObservation(){wire=input->Mono.just(response(401,""));var value=execution().execute(original(false)).block();assertEquals(StaffPostingResponse.Status.CURRENT_INVOCATION_REFUSED,value.status());assertEquals(401,value.domainStatus());assertNull(value.ownerStatus());}
    @Test void missingRecoveryEvidenceIsAnObservationNotANoEffectClosure(){wire=input->Mono.just(response(404,outcome("NOT_OBSERVED",404)));var value=execution().execute(original(true)).block();assertEquals(StaffPostingResponse.Status.NOT_OBSERVED,value.status());assertEquals(UUID.fromString(ACTION),value.actionId());assertNull(value.receipt());}
    @Test void recoveryCannotAcceptAnUnrecoveredReceipt(){assertEquals(StaffPostingResponse.Status.OUTCOME_UNKNOWN,execution().execute(original(true)).block().status());}
    @Test void changedCurrentCallerAfterDomainCommitWithholdsReceiptWithoutLosingKnownCommit(){wire=input->{current.set(false);return Mono.just(response(200,committed(false)));};var value=execution().execute(original(false)).block();assertEquals(StaffPostingResponse.Status.COMMITTED_RESPONSE_UNAVAILABLE,value.status());assertNull(value.receipt());assertEquals(UUID.fromString(ACTION),value.actionId());}
    @Test void stalledDomainInvocationIsFiniteAndNeverAutomaticallyRetried(){wire=input->Mono.never();StepVerifier.withVirtualTime(()->execution().execute(original(false))).thenAwait(Duration.ofSeconds(6)).assertNext(value->assertEquals(StaffPostingResponse.Status.OUTCOME_UNKNOWN,value.status())).verifyComplete();assertEquals(1,calls.get());}
}
