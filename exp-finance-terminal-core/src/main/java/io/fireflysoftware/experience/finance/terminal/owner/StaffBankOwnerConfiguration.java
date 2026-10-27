/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.owner;
import io.fireflysoftware.experience.finance.terminal.owner.impl.*;
import io.fireflysoftware.domain.organization.sdk.invoker.ApiClient;
import org.fireflyframework.security.spi.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.*;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.*;import org.springframework.security.oauth2.client.registration.*;import org.springframework.security.oauth2.core.*;import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;import org.springframework.security.oauth2.jwt.*;
import java.time.Clock;import java.util.function.LongSupplier;
@Configuration(proxyBeanMethods=false)
@ConditionalOnProperty(name="firefly.terminal.bank-owner.enabled",havingValue="true",matchIfMissing=false)
public class StaffBankOwnerConfiguration{
 @Bean public StaffBankOwnerReadService staffBankOwnerReadService(SecurityContextPort contexts,PolicyDecisionPort policies,ReactiveOAuth2AuthorizedClientManager clients,ReactiveClientRegistrationRepository registrations,Environment environment){
  if(!Boolean.TRUE.equals(environment.getProperty("firefly.security.resource-server.enabled",Boolean.class))||!Boolean.TRUE.equals(environment.getProperty("firefly.security.resource-server.strict-policy",Boolean.class))||!Boolean.TRUE.equals(environment.getProperty("firefly.security.resource-server.tenant-required",Boolean.class))||environment.getProperty("firefly.terminal.security.issuer","").isBlank()||environment.getProperty("firefly.terminal.security.audience","").isBlank()||!environment.getProperty("firefly.terminal.security.jwk-set-uri","").startsWith("https://"))throw new IllegalStateException("Configured strict signed staff trust and tenant projection required");
  var profile=Binder.get(environment).bind("firefly.terminal.bank-owner.machine",Bindable.of(BankOwnerDomainRegistration.class)).orElseThrow(()->new IllegalStateException("Explicit exact domain machine registration required"));
  var decoder=NimbusReactiveJwtDecoder.withJwkSetUri(profile.jwksUri().toString()).jwsAlgorithm(SignatureAlgorithm.RS256).build();
  OAuth2TokenValidator<Jwt> audience=jwt->jwt.getAudience().contains(profile.audience())?OAuth2TokenValidatorResult.success():OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(profile.issuer()),audience));
  Clock clock=Clock.systemUTC();LongSupplier nanos=System::nanoTime;
  var source=new DomainBankOwnerClient(profile,clients,registrations,decoder,new ApiClient().getWebClient(),clock,nanos);
  return new StaffBankOwnerReadServiceImpl(contexts,policies,source,clock,nanos);
 }
}
