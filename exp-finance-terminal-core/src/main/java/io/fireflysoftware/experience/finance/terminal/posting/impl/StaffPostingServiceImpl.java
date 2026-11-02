/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.posting.impl;
import io.fireflysoftware.experience.finance.terminal.posting.*;
import io.fireflysoftware.domain.accounting.sdk.invoker.ApiClient;
import io.fireflysoftware.domain.accounting.sdk.model.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
/** Immutable request capture, no financial store, no new action and no automatic retry. */
public final class StaffPostingServiceImpl implements StaffPostingService {
    private static final ObjectMapper JSON=ApiClient.createDefaultMapper(ApiClient.createDefaultDateFormat()).enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
        .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,DeserializationFeature.FAIL_ON_TRAILING_TOKENS,DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
        .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT).disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
    private final StaffPostingExecution execution;
    public StaffPostingServiceImpl(StaffPostingExecution execution){this.execution=Objects.requireNonNull(execution);}
    @Override public Mono<StaffPostingResponse> submit(String tenant,String book,byte[] completeRequest,boolean recovery){
        final StaffPostingInvocation captured;
        try{
            if(completeRequest==null||completeRequest.length==0||completeRequest.length>1048576)throw new IllegalArgumentException();
            var root=JSON.readTree(completeRequest);var fields=new HashSet<String>();root.fieldNames().forEachRemaining(fields::add);if(!root.isObject()||!fields.equals(Set.of("expectedIntentDigest","posting")))throw new IllegalArgumentException();
            var input=JSON.treeToValue(root,ManagedPostingRequest.class);var posting=Objects.requireNonNull(input.getPosting());
            if(posting.getSource()==null||!"LEDGER".equals(posting.getSource().getOwner())||!"COMMIT".equals(posting.getSource().getEventKind())||!Long.valueOf(1).equals(posting.getSource().getRevision())||!Integer.valueOf(0).equals(posting.getSource().getEffectOrdinal())||posting.getEntries()==null||posting.getEntries().size()!=1||posting.getEntries().getFirst().getLines()==null||posting.getEntries().getFirst().getLines().size()!=2)throw new IllegalArgumentException();
            for(var line:posting.getEntries().getFirst().getLines())if(line.getCurrency()==null||!"USD".equals(line.getCurrency().getIsoCode())||!Integer.valueOf(2).equals(line.getPostedScale()))throw new IllegalArgumentException();
            captured=new StaffPostingInvocation(tenant,book,UUID.fromString(posting.getActionId()),input.getExpectedIntentDigest(),JSON.writeValueAsString(input),recovery);
        }catch(Exception invalid){return Mono.error(new StaffPostingPreDispatchException(HttpStatus.BAD_REQUEST));}
        return Mono.defer(()->execution.execute(captured)).switchIfEmpty(Mono.fromSupplier(()->unknown(captured))).onErrorResume(error->error instanceof StaffPostingPreDispatchException?Mono.error(error):Mono.just(unknown(captured)));
    }
    private static StaffPostingResponse unknown(StaffPostingInvocation value){return new StaffPostingResponse(value.actionId(),value.expectedIntentDigest(),StaffPostingResponse.Status.OUTCOME_UNKNOWN,null,null,null);}
}
