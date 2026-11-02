/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.owner.impl;
import io.fireflysoftware.experience.finance.terminal.owner.*;
import org.fireflyframework.core.transaction.R2dbcOperationBudget;
import org.fireflyframework.security.api.domain.SecurityPrincipal;
import org.fireflyframework.security.spi.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.time.*;
import java.util.*;
import java.util.function.LongSupplier;
public final class StaffBankOwnerReadServiceImpl implements StaffBankOwnerReadService {
 private static final Duration MAXIMUM=Duration.ofSeconds(10);
 private final SecurityContextPort contexts; private final PolicyDecisionPort policies; private final OrganizationBankOwnerPort source; private final Clock clock; private final LongSupplier nanos;
 public StaffBankOwnerReadServiceImpl(SecurityContextPort contexts,PolicyDecisionPort policies,OrganizationBankOwnerPort source,Clock clock,LongSupplier nanos){this.contexts=Objects.requireNonNull(contexts);this.policies=Objects.requireNonNull(policies);this.source=Objects.requireNonNull(source);this.clock=Objects.requireNonNull(clock);this.nanos=Objects.requireNonNull(nanos);}
 @Override public Mono<StaffBankOwnerView> current(StaffBankOwnerQuery query){return Mono.defer(()->{
  Objects.requireNonNull(query);long before=nanos.getAsLong();Instant started=clock.instant();var initial=budget(started,started.plus(MAXIMUM),before);
  return contexts.currentPrincipal().switchIfEmpty(Mono.error(forbidden())).flatMap(actor->{
   requireActor(actor,query);Instant callerUntil=(Instant)actor.claims().get("exp");Instant originalUntil=earliest(started.plus(MAXIMUM),callerUntil);var original=budget(started,originalUntil,before);
   return authorize(actor,query,original).then(Mono.defer(()->source.read(query,original,originalUntil))).switchIfEmpty(Mono.error(unavailable())).flatMap(observation->{
    var view=observation.view();
    if(!view.matches(query)||view.currentUntilEpochMillis()<=clock.millis())return Mono.error(unavailable());
    var delivery=budget(started,earliest(earliest(originalUntil,Instant.ofEpochMilli(view.observationUntilEpochMillis())),Instant.ofEpochMilli(view.currentUntilEpochMillis()).minusMillis(1)),before).intersect(observation.window());
    return contexts.currentPrincipal().filter(actor::equals).switchIfEmpty(Mono.error(forbidden())).flatMap(current->authorize(current,query,delivery)).then(Mono.fromSupplier(()->{Instant sampled=clock.instant();long handoff=sampled.plus(delivery.remaining()).toEpochMilli();return view.withObservationUntil(handoff);})).timeout(delivery.remaining());
   }).timeout(original.remaining());
  }).timeout(initial.remaining());
 }).onErrorMap(error->error instanceof ResponseStatusException?error:unavailable());}
 private Mono<Void> authorize(SecurityPrincipal actor,StaffBankOwnerQuery query,R2dbcOperationBudget budget){return Mono.defer(()->{
  requireActor(actor,query);budget.remaining();return policies.authorize(actor,"terminal.finance.bank-owner.current.read",query.resource(),Map.of("actorId",actor.attributes().get("staffActorId"),"tenantId",query.tenantId().toString(),"bankId",query.bankId().toString(),"enrollmentId",query.enrollmentId().toString(),"version",Long.toString(query.version()),"purpose","ADMINISTRATIVE_CURRENT_OWNER_READ")).filter(d->d.granted()&&d.obligations().isEmpty()).switchIfEmpty(Mono.error(forbidden())).timeout(budget.remaining()).doOnNext(d->{requireActor(actor,query);budget.remaining();}).then();});}
 private void requireActor(SecurityPrincipal actor,StaffBankOwnerQuery query){if(actor==null||actor.subject()==null||actor.subject().isBlank()||actor.issuer()==null||actor.issuer().isBlank()||!query.tenantId().toString().equals(actor.tenantId())||!validStaffActor(actor.attributes().get("staffActorId"))||!(actor.claims().get("exp") instanceof Instant expires)||!expires.isAfter(clock.instant()))throw forbidden();}
 private static boolean validStaffActor(Object value){if(!(value instanceof String actor)||actor.length()!=36)return false;try{return UUID.fromString(actor).toString().equals(actor);}catch(IllegalArgumentException error){return false;}}
 private R2dbcOperationBudget budget(Instant started,Instant until,long before){var v=R2dbcOperationBudget.observed(started,until,before,MAXIMUM,clock,nanos);v.remaining();return v;}
 private static Instant earliest(Instant a,Instant b){return a.isBefore(b)?a:b;}
 private static ResponseStatusException forbidden(){return new ResponseStatusException(HttpStatus.FORBIDDEN,"Current exact administrative owner-read authority required");}
 private static ResponseStatusException unavailable(){return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Current owner observation unavailable");}
}
