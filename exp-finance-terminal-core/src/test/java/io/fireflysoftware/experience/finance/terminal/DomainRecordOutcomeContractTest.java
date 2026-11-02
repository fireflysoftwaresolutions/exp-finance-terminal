/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal;

import io.fireflysoftware.experience.finance.terminal.impl.DomainRecordClientImpl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Genuine original-generated SDK over controlled HTTP; this is not persisted owner or STAFF authority acceptance. */
class DomainRecordOutcomeContractTest {
    private static final UUID ID=UUID.fromString("10000000-0000-4000-8000-000000000004");
    private static final UUID ACTOR=UUID.fromString("20000000-0000-4000-8000-000000000001");
    private static final UUID OWNER_ACTOR=UUID.fromString("20000000-0000-4000-8000-000000000002");
    private static final Duration TIMEOUT=Duration.ofSeconds(3);
    record Reply(int status,String body){}
    private static RecordCommand command(FinanceArea area){return new RecordCommand(area,ID,FinanceSamples.CUSTOMER_PARTY,
        "Caller reason","Caller note",null,null,null,ACTOR,"CLOSED","APPROVE");}
    static final class Fixture implements AutoCloseable {
        final HttpServer server;final DomainRecordClientImpl client;final AtomicInteger calls=new AtomicInteger();
        final AtomicReference<String> writeKey=new AtomicReference<>();final AtomicReference<String> requestKey=new AtomicReference<>();final AtomicReference<String> verb=new AtomicReference<>();final AtomicReference<String> body=new AtomicReference<>();
        Fixture(Function<HttpExchange,Reply> response)throws Exception{
            server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
            server.createContext("/",request->{try{
                calls.incrementAndGet();requestKey.set(request.getRequestHeaders().getFirst("X-Idempotency-Key"));if(!request.getRequestMethod().equals("GET"))writeKey.set(requestKey.get());verb.set(request.getRequestMethod());body.set(new String(request.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
                Reply reply=response.apply(request);byte[] bytes=reply.body().getBytes(StandardCharsets.UTF_8);
                request.getResponseHeaders().set("Content-Type","application/json");
                request.sendResponseHeaders(reply.status(),reply.status()==204?-1:bytes.length);
                if(reply.status()!=204)request.getResponseBody().write(bytes);
            }finally{request.close();}});server.start();String base="http://127.0.0.1:"+server.getAddress().getPort();
            var accounts=new io.fireflysoftware.domain.banking.accounts.sdk.invoker.ApiClient();accounts.setBasePath(base);
            var cards=new io.fireflysoftware.domain.banking.cards.sdk.invoker.ApiClient();cards.setBasePath(base);
            var loans=new io.fireflysoftware.domain.lending.loan.origination.sdk.invoker.ApiClient();loans.setBasePath(base);
            var mortgage=new io.fireflysoftware.domain.lending.mortgage.sdk.invoker.ApiClient();mortgage.setBasePath(base);
            var scf=new io.fireflysoftware.domain.lending.supplychainfinance.sdk.invoker.ApiClient();scf.setBasePath(base);
            var collateral=new io.fireflysoftware.domain.lending.collateral.sdk.invoker.ApiClient();collateral.setBasePath(base);
            var collections=new io.fireflysoftware.domain.lending.collections.sdk.invoker.ApiClient();collections.setBasePath(base);
            var provisioning=new io.fireflysoftware.domain.lending.provisioning.sdk.invoker.ApiClient();provisioning.setBasePath(base);
            var compliance=new io.fireflysoftware.domain.lending.compliance.sdk.invoker.ApiClient();compliance.setBasePath(base);
            client=new DomainRecordClientImpl(new SandboxConfigurationStore(FinanceSamples.properties(),FinanceSamples.currencies(),FinanceSamples.countries()),
                Set.of("accounts","cards","lending-applications","mortgage-agreements","supply-chain-finance-agreements","collateral-cases","collection-cases","provisioning-cases","aml-cases"),
                new io.fireflysoftware.domain.banking.accounts.sdk.api.AccountBackofficeApi(accounts),
                new io.fireflysoftware.domain.banking.accounts.sdk.api.AccountHoldersApi(accounts),new io.fireflysoftware.domain.banking.accounts.sdk.api.AccountsApi(accounts),
                new io.fireflysoftware.domain.banking.cards.sdk.api.CardBackofficeApi(cards),new io.fireflysoftware.domain.banking.cards.sdk.api.CardsApi(cards),
                new io.fireflysoftware.domain.lending.loan.origination.sdk.api.LoanOriginationApi(loans),
                new io.fireflysoftware.domain.lending.mortgage.sdk.api.MortgageAgreementsApi(mortgage),
                new io.fireflysoftware.domain.lending.supplychainfinance.sdk.api.SupplyChainFinanceAgreementsApi(scf),
                new io.fireflysoftware.domain.lending.collateral.sdk.api.CollateralCasesApi(collateral),
                new io.fireflysoftware.domain.lending.collections.sdk.api.CollectionCasesApi(collections),
                new io.fireflysoftware.domain.lending.provisioning.sdk.api.ProvisioningCasesApi(provisioning),
                new io.fireflysoftware.domain.lending.compliance.sdk.api.AmlCasesApi(compliance));
        }
        public void close(){server.stop(0);}
    }
    @Test void accountCloseReturnsObservedOwnerStateInsteadOfCommandedClosed()throws Exception{
        try(var fixture=new Fixture(request->new Reply(200,request.getRequestMethod().equals("GET")?
            "{\"accountId\":\""+ID+"\",\"accountNumber\":\"OWNER-ACC\",\"accountStatus\":\"OPEN\"}":""))){
            var returned=fixture.client.close(command(FinanceArea.ACCOUNT)).block(TIMEOUT);
            assertEquals("OPEN",returned.status());assertEquals(2,fixture.calls.get());assertNull(returned.actorId());assertNull(returned.customerPartyId());
        }
    }
    @Test void cardCancellationReturnsObservedOwnerStateInsteadOfCommandedClosed()throws Exception{
        try(var fixture=new Fixture(request->new Reply(200,request.getRequestMethod().equals("GET")?
            "{\"cardId\":\""+ID+"\",\"maskedCardNumber\":\"****1234\",\"cardStatus\":\"ACTIVE\"}":""))){
            var returned=fixture.client.close(command(FinanceArea.CARD)).block(TIMEOUT);
            assertEquals("ACTIVE",returned.status());assertEquals(2,fixture.calls.get());assertNull(returned.actorId());
        }
    }
    @Test void untypedLoanApprovalReplyCannotProveApproved()throws Exception{
        try(var fixture=new Fixture(request->new Reply(200,"{}"))){
            assertThrows(RuntimeException.class,()->fixture.client.decide(command(FinanceArea.LENDING_APPLICATION)).block(TIMEOUT));
            assertEquals(1,fixture.calls.get());
        }
    }
    @Test void anotherOwnerRecordCannotConfirmTheRequestedClose()throws Exception{
        try(var fixture=new Fixture(request->new Reply(200,"{\"collateralCaseId\":\"10000000-0000-4000-8000-000000000009\",\"collateralStatus\":\"RELEASED\"}"))){
            assertThrows(RuntimeException.class,()->fixture.client.close(command(FinanceArea.COLLATERAL_CASE)).block(TIMEOUT));assertEquals(1,fixture.calls.get());
        }
    }
    @Test void missingOwnerStatusCannotAuthorizeDraftRemoval()throws Exception{
        try(var fixture=new Fixture(request->new Reply(request.getRequestMethod().equals("DELETE")?204:200,"{\"id\":\""+ID+"\"}"))){
            assertThrows(RuntimeException.class,()->fixture.client.removeDraft(command(FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT),"DRAFT").block(TIMEOUT));
            assertEquals(1,fixture.calls.get());assertEquals("GET",fixture.verb.get());
        }
    }
    @Test void updateReturnsOwnerActorAndDoesNotEchoUnpersistedPartyOrNote()throws Exception{
        try(var fixture=new Fixture(request->new Reply(200,"{\"mortgageAgreementId\":\""+ID+"\",\"agreementStatus\":\"ACTIVE\",\"updatedBy\":\""+OWNER_ACTOR+"\"}"))){
            var returned=fixture.client.update(command(FinanceArea.MORTGAGE_AGREEMENT)).block(TIMEOUT);
            assertEquals(OWNER_ACTOR,returned.actorId());assertNull(returned.customerPartyId());assertNull(returned.note());
            assertEquals(2,fixture.calls.get());assertEquals("PUT",fixture.verb.get());assertTrue(fixture.body.get().contains(ACTOR.toString()));
        }
    }
    @Test void unavailableOwnerCannotReturnASyntheticSuccess()throws Exception{
        try(var fixture=new Fixture(request->new Reply(503,"{}"))){
            assertThrows(RuntimeException.class,()->fixture.client.close(command(FinanceArea.COLLATERAL_CASE)).block(TIMEOUT));assertEquals(1,fixture.calls.get());
        }
    }
}
