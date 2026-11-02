/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal;
import io.fireflysoftware.experience.finance.terminal.posting.*;
import io.fireflysoftware.experience.finance.terminal.posting.impl.*;
import java.util.Objects;
import org.fireflyframework.security.spi.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
@Configuration(proxyBeanMethods=false)
@ConditionalOnProperty(name="firefly.terminal.managed-posting.enabled",havingValue="true")
@EnableConfigurationProperties(StaffPostingSettings.class)
public class StaffPostingConfiguration {
    @Bean StaffPostingClientFactory staffPostingClients(StaffPostingSettings settings,ManagedPostingStaffTrust trust,SecurityContextPort security,PolicyDecisionPort policies,ReactiveClientRegistrationRepository registrations,@Qualifier("terminalJwtDecoder")ReactiveJwtDecoder decoder){
        if(!trust.enabled()||!Objects.equals(settings.issuer(),trust.issuer())||!Objects.equals(settings.experienceAudience(),trust.audience())||!Objects.equals(settings.inboundClientId(),trust.clientId())||!Objects.equals(settings.platformTenant(),trust.platformTenant())||!Objects.equals(settings.resourceTenantId(),trust.resourceTenantId())||!Objects.equals(settings.bookId(),trust.bookId())||!Objects.equals(settings.tenantClaim(),trust.tenantClaim())||!Objects.equals(settings.clientClaim(),trust.clientClaim())||!Objects.equals(settings.kindClaim(),trust.kindClaim())||!Objects.equals(settings.staffClaim(),trust.staffClaim())||!Objects.equals(settings.scopeClaim(),trust.scopeClaim()))throw new IllegalStateException("Same explicit native managed-posting receiver and Domain delegation trust required");
        return new ExchangedStaffPostingClientFactoryImpl(settings,security,policies,registrations,decoder);
    }
    @Bean StaffPostingExecution staffPostingExecution(StaffPostingClientFactory clients){return new GeneratedStaffPostingExecutionImpl(clients);}
    @Bean StaffPostingService staffPostingService(StaffPostingExecution execution){return new StaffPostingServiceImpl(execution);}
}
