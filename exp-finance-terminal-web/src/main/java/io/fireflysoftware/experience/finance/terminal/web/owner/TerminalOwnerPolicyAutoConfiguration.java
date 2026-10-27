/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.web.owner;
import org.fireflyframework.security.adapter.opa.OpaPolicyDecisionAdapter;
import org.fireflyframework.security.rs.ResourceServerAutoConfiguration;
import org.fireflyframework.security.spi.PolicyDecisionPort;
import org.springframework.beans.factory.InitializingBean;import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.bind.*;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Set;
@AutoConfiguration(before=ResourceServerAutoConfiguration.class,afterName="org.springframework.boot.security.oauth2.client.autoconfigure.reactive.ReactiveOAuth2ClientAutoConfiguration")
@ConditionalOnProperty(name="firefly.terminal.bank-owner.enabled",havingValue="true",matchIfMissing=false)
@Import(TerminalOwnerPolicyAutoConfiguration.DefaultPolicyBackend.class)
public class TerminalOwnerPolicyAutoConfiguration{
 @Bean public InitializingBean terminalOwnerNativeClientConfiguration(ObjectProvider<OAuth2ClientProperties> clients,Environment environment){return()->{
  var configured=clients.getIfAvailable();if(configured==null)return;String id=environment.getProperty("firefly.terminal.bank-owner.machine.registration-id");if(id==null)return;var registration=configured.getRegistration().get(id);if(registration==null)return;
  String method=registration.getClientAuthenticationMethod();String expected=environment.getProperty("firefly.terminal.bank-owner.machine.client-id");
  if(!"client_credentials".equals(registration.getAuthorizationGrantType())||method!=null&&!Set.of("client_secret_basic","client_secret_post").contains(method)||!resolved(registration.getClientId(),environment)||!registration.getClientId().equals(expected)||!resolved(registration.getClientSecret(),environment))throw new IllegalStateException("Explicit matching current-owner OAuth client credentials required");
 };}
 private static boolean resolved(String value,Environment environment){if(value==null||value.isBlank())return false;try{return value.equals(environment.resolveRequiredPlaceholders(value));}catch(IllegalArgumentException invalid){return false;}}
 @ConditionalOnMissingBean(PolicyDecisionPort.class)
 static class DefaultPolicyBackend{
  @Bean PolicyDecisionPort terminalOwnerPolicyDecisionPort(Environment environment){var p=Binder.get(environment).bind("firefly.terminal.bank-owner.policy",Bindable.of(TerminalOwnerPolicyProperties.class)).orElseThrow(()->new IllegalStateException("Explicit current-owner policy endpoint required"));return new OpaPolicyDecisionAdapter(WebClient.builder().baseUrl(p.baseUri().toString()).codecs(c->c.defaultCodecs().maxInMemorySize(16*1024)).build(),p.decisionPath());}
 }
}
