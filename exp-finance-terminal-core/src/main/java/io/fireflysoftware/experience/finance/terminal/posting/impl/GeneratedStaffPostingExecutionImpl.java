/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.posting.impl;
import io.fireflysoftware.experience.finance.terminal.posting.*;
import io.fireflysoftware.domain.accounting.sdk.invoker.ApiClient;
import io.fireflysoftware.domain.accounting.sdk.model.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
/** One genuine Domain SDK call; Experience never reaches Core or persists financial state. */
public final class GeneratedStaffPostingExecutionImpl implements StaffPostingExecution {
    private static final ObjectMapper JSON=ApiClient.createDefaultMapper(ApiClient.createDefaultDateFormat()).enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION).enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private final StaffPostingClientFactory clients;
    public GeneratedStaffPostingExecutionImpl(StaffPostingClientFactory clients){this.clients=Objects.requireNonNull(clients);}
    @Override public Mono<StaffPostingResponse> execute(StaffPostingInvocation original){return Mono.defer(()->{
        final ManagedPostingRequest input;
        try{input=JSON.readValue(original.originalRequestJson(),ManagedPostingRequest.class);if(input.getPosting()==null||!original.actionId().toString().equals(input.getPosting().getActionId())||!original.expectedIntentDigest().equals(input.getExpectedIntentDigest()))throw new IllegalArgumentException();}
        catch(Exception invalid){return Mono.error(new StaffPostingPreDispatchException(HttpStatus.BAD_REQUEST));}
        return Mono.defer(()->clients.current(original.tenant(),original.book(),original.recovery())).switchIfEmpty(Mono.error(new StaffPostingPreDispatchException(HttpStatus.SERVICE_UNAVAILABLE))).onErrorMap(GeneratedStaffPostingExecutionImpl::preDispatch)
            .flatMap(client->Mono.defer(client::revalidate).onErrorMap(GeneratedStaffPostingExecutionImpl::preDispatch).then(Mono.defer(()->call(client,original,input))));
    });}
    private Mono<StaffPostingResponse> call(StaffPostingClientFactory.Client client,StaffPostingInvocation original,ManagedPostingRequest input){
        return Mono.defer(()->original.recovery()?client.api().recoverLedgerJournalWithHttpInfo(original.tenant(),original.book(),input):client.api().postLedgerJournalWithHttpInfo(original.tenant(),original.book(),input))
            .timeout(client.remaining()).map(reply->project(original,reply.getStatusCode().value(),reply.getBody())).switchIfEmpty(Mono.fromSupplier(()->unknown(original)))
            .onErrorResume(error->Mono.just(failure(original,error))).flatMap(value->{
                if(value.status()!=StaffPostingResponse.Status.COMMITTED)return Mono.just(value);
                return Mono.defer(client::revalidate).thenReturn(value).onErrorReturn(outcome(original,StaffPostingResponse.Status.COMMITTED_RESPONSE_UNAVAILABLE,value.domainStatus(),value.ownerStatus(),null));
            });
    }
    private static StaffPostingResponse project(StaffPostingInvocation original,int status,ManagedPostingResponse body){
        if(body==null||!original.actionId().equals(body.getActionId())||!original.expectedIntentDigest().equals(body.getIntentDigest())||body.getStatus()==null)return unknown(original);
        var kind=StaffPostingResponse.Status.valueOf(body.getStatus().name());var receipt=body.getReceipt();
        if(kind==StaffPostingResponse.Status.COMMITTED){
            if(status!=200||!Integer.valueOf(200).equals(body.getOwnerStatus())||receipt==null||!original.actionId().equals(receipt.getActionId())||!original.expectedIntentDigest().equals(receipt.getIntentDigest())||receipt.getReceiptId()==null||receipt.getReceiptId().equals(new UUID(0,0))||receipt.getPostingId()==null||receipt.getPostingId().equals(new UUID(0,0))||receipt.getJournalSequence()==null||receipt.getJournalSequence()<=0||receipt.getRecordedAt()==null||receipt.getRecovered()==null||original.recovery()&&!receipt.getRecovered())return unknown(original);
            return outcome(original,kind,status,body.getOwnerStatus(),new StaffPostingReceipt(receipt.getReceiptId(),receipt.getActionId(),receipt.getPostingId(),receipt.getJournalSequence(),receipt.getIntentDigest(),receipt.getRecovered(),receipt.getRecordedAt()));
        }
        if(receipt!=null)return unknown(original);
        if(kind==StaffPostingResponse.Status.CURRENT_INVOCATION_REFUSED&&(status<400||status>=500||status==408||status==429||!Integer.valueOf(status).equals(body.getOwnerStatus())))return unknown(original);
        if(kind==StaffPostingResponse.Status.NOT_OBSERVED&&(!original.recovery()||status!=404||!Integer.valueOf(404).equals(body.getOwnerStatus())))return unknown(original);
        if((kind==StaffPostingResponse.Status.OUTCOME_UNKNOWN||kind==StaffPostingResponse.Status.COMMITTED_RESPONSE_UNAVAILABLE)&&status!=503)return unknown(original);
        return outcome(original,kind,status,body.getOwnerStatus(),null);
    }
    private static StaffPostingResponse failure(StaffPostingInvocation original,Throwable error){
        if(error instanceof WebClientResponseException response){
            int status=response.getStatusCode().value();byte[] raw=response.getResponseBodyAsByteArray();
            if(raw.length>0&&raw.length<=1048576){
                try{return project(original,status,JSON.readValue(raw,ManagedPostingResponse.class));}catch(Exception notObservation){
                    try{var before=JSON.readValue(raw,ManagedPostingPreDispatchError.class);if(before.getStatus()!=null&&before.getStatus()==status&&before.getCode()!=null&&"MANAGED_POSTING_PRE_DISPATCH_REJECTED".equals(before.getCode().toString())&&before.getPhase()!=null&&"PRE_DISPATCH".equals(before.getPhase().toString()))return outcome(original,StaffPostingResponse.Status.DOMAIN_PRE_DISPATCH_REFUSED,status,null,null);}catch(Exception unavailable){}
                }
            }
            if(status>=400&&status<500&&status!=408&&status!=429)return outcome(original,StaffPostingResponse.Status.CURRENT_INVOCATION_REFUSED,status,null,null);
        }
        return unknown(original);
    }
    private static StaffPostingResponse outcome(StaffPostingInvocation original,StaffPostingResponse.Status status,Integer domain,Integer owner,StaffPostingReceipt receipt){return new StaffPostingResponse(original.actionId(),original.expectedIntentDigest(),status,domain,owner,receipt);}
    private static StaffPostingResponse unknown(StaffPostingInvocation original){return outcome(original,StaffPostingResponse.Status.OUTCOME_UNKNOWN,null,null,null);}
    private static Throwable preDispatch(Throwable error){return error instanceof StaffPostingPreDispatchException?error:new StaffPostingPreDispatchException(error instanceof ResponseStatusException known?known.getStatusCode():HttpStatus.SERVICE_UNAVAILABLE);}
}
