/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.owner.impl;
import io.fireflysoftware.experience.finance.terminal.owner.*;
import io.fireflysoftware.domain.organization.sdk.api.BankEnrollmentOwnerApi;
import io.fireflysoftware.domain.organization.sdk.invoker.ApiClient;
import io.fireflysoftware.domain.organization.sdk.model.CurrentBankOwnerView;
import org.fireflyframework.core.transaction.R2dbcOperationBudget;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.reactive.function.client.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.time.*;import java.util.*;import java.util.function.LongSupplier;import java.util.stream.Collectors;

public final class DomainBankOwnerClient implements OrganizationBankOwnerPort {
 private final BankOwnerDomainRegistration profile;private final ReactiveOAuth2AuthorizedClientManager clients;
 private final ReactiveClientRegistrationRepository registrations;private final ReactiveJwtDecoder decoder;
 private final WebClient transport;private final Clock clock;private final LongSupplier nanos;
 public DomainBankOwnerClient(BankOwnerDomainRegistration profile,ReactiveOAuth2AuthorizedClientManager clients,
   ReactiveClientRegistrationRepository registrations,ReactiveJwtDecoder decoder,WebClient transport,Clock clock,LongSupplier nanos){
  this.profile=Objects.requireNonNull(profile);this.clients=Objects.requireNonNull(clients);this.registrations=Objects.requireNonNull(registrations);this.decoder=Objects.requireNonNull(decoder);this.transport=Objects.requireNonNull(transport);this.clock=Objects.requireNonNull(clock);this.nanos=Objects.requireNonNull(nanos);
 }
 @Override public Mono<Observation> read(StaffBankOwnerQuery query,R2dbcOperationBudget original,Instant originalUntil){return Mono.defer(()->{
  original.remaining();if(originalUntil==null||!originalUntil.isAfter(clock.instant()))return Mono.error(unavailable());if(!profile.tenantId().equals(query.tenantId())||!profile.bankId().equals(query.bankId()))return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,"Configured owner-read scope required"));
  return registrations.findByRegistrationId(profile.registrationId()).filter(r->AuthorizationGrantType.CLIENT_CREDENTIALS.equals(r.getAuthorizationGrantType())&&profile.clientId().equals(r.getClientId())&&profile.tokenUri().toString().equals(r.getProviderDetails().getTokenUri())).switchIfEmpty(Mono.error(unavailable()))
   .flatMap(registration->clients.authorize(OAuth2AuthorizeRequest.withClientRegistrationId(profile.registrationId()).principal("organization-owner-read/"+profile.registrationId()).build()).filter(c->registration.equals(c.getClientRegistration())).switchIfEmpty(Mono.error(unavailable())))
   .flatMap(client->{var token=client.getAccessToken();if(token==null||!OAuth2AccessToken.TokenType.BEARER.equals(token.getTokenType())||token.getExpiresAt()==null||!token.getExpiresAt().isAfter(clock.instant()))return Mono.error(unavailable());
    return decoder.decode(token.getTokenValue()).flatMap(jwt->{machine(jwt);Instant until=earliest(token.getExpiresAt(),jwt.getExpiresAt());var window=narrow(original,until);return request(query,token.getTokenValue(),window,originalUntil);});
   }).timeout(original.remaining());
 }).onErrorMap(error->error instanceof ResponseStatusException?error:unavailable());}
 private Mono<Observation> request(StaffBankOwnerQuery query,String token,R2dbcOperationBudget window,Instant originalUntil){return Mono.defer(()->{
  window.remaining();var api=new ApiClient(transport.mutate().codecs(c->c.defaultCodecs().maxInMemorySize(4*1024*1024)).defaultHeader(HttpHeaders.AUTHORIZATION,"Bearer "+token).build());api.setBasePath(profile.baseUri().toString().replaceAll("/$",""));
  return new BankEnrollmentOwnerApi(api).getCurrentBankOwnerWithHttpInfo(query.tenantId(),query.bankId(),query.enrollmentId(),query.version(),originalUntil.toEpochMilli()).timeout(window.remaining())
   .onErrorMap(WebClientResponseException.class,e->e.getStatusCode().value()==409?new ResponseStatusException(HttpStatus.CONFLICT,"Exact owner is not current"):unavailable())
   .map(response->{window.remaining();var type=response.getHeaders().getContentType();if(response.getStatusCode().value()!=200||type==null||!MediaType.APPLICATION_JSON.isCompatibleWith(type)||Arrays.stream(response.getHeaders().getCacheControl().split(",")).map(String::trim).noneMatch("no-store"::equals))throw unavailable();var dto=response.getBody();
    if(dto==null||dto.getCurrentUntilEpochMillis()==null||dto.getObservationUntilEpochMillis()==null||dto.getCurrentUntilEpochMillis()<=clock.millis()||dto.getCurrentUntilEpochMillis()>clock.instant().plusSeconds(30).toEpochMilli()||dto.getObservationUntilEpochMillis()<=clock.millis()||dto.getObservationUntilEpochMillis()>dto.getCurrentUntilEpochMillis()||dto.getObservationUntilEpochMillis()>clock.instant().plusSeconds(10).toEpochMilli())throw unavailable();
    var view=new StaffBankOwnerView(dto.getTenantId(),dto.getBankId(),dto.getEnrollmentId(),dto.getVersion(),dto.getInstitutionId(),dto.getIssuerAuthorityId(),Set.copyOf(dto.getAllowedUses()),dto.getControlVersion(),dto.getOwnershipGeneration(),dto.getBankGeneration(),dto.getSourceGeneration(),dto.getCurrentUntilEpochMillis(),dto.getObservationUntilEpochMillis());if(!view.matches(query))throw unavailable();
    var nativeWindow=narrow(window,earliest(Instant.ofEpochMilli(view.currentUntilEpochMillis()).minusMillis(1),Instant.ofEpochMilli(view.observationUntilEpochMillis())));Instant sampled=clock.instant();long handoff=Math.min(view.currentUntilEpochMillis(),sampled.plus(nativeWindow.remaining()).toEpochMilli());return new Observation(view.withObservationUntil(handoff),nativeWindow);
   });
 });}
 private void machine(Jwt jwt){if(!profile.issuer().equals(jwt.getClaimAsString("iss"))||!profile.subject().equals(jwt.getSubject())||!jwt.getAudience().contains(profile.audience())||!profile.machineTenantId().equals(jwt.getClaims().get("tenant_id"))||!profile.clientId().equals(jwt.getClaims().get("client_id"))||!"MACHINE".equals(jwt.getClaims().get("principal_kind"))||jwt.getClaims().containsKey("staff_actor_id")||jwt.getExpiresAt()==null||!jwt.getExpiresAt().isAfter(clock.instant())||jwt.getNotBefore()!=null&&jwt.getNotBefore().isAfter(clock.instant()))throw unavailable();}
 private R2dbcOperationBudget narrow(R2dbcOperationBudget original,Instant until){long before=nanos.getAsLong();Instant sampled=clock.instant();var window=R2dbcOperationBudget.observed(sampled,earliest(until,sampled.plusSeconds(10)),before,Duration.ofSeconds(10),clock,nanos).intersect(original);window.remaining();return window;}
 private static Instant earliest(Instant a,Instant b){return a.isBefore(b)?a:b;}
 private static ResponseStatusException unavailable(){return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Current domain owner observation unavailable");}
}
