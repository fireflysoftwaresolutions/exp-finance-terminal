/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.web.posting;
import io.fireflysoftware.experience.finance.terminal.web.owner.TerminalOwnerPolicyAutoConfiguration;
import io.fireflysoftware.experience.finance.terminal.web.owner.TerminalOwnerPolicyProperties;
import org.fireflyframework.security.adapter.opa.OpaPolicyDecisionAdapter;
import org.fireflyframework.security.rs.ResourceServerAutoConfiguration;
import org.fireflyframework.security.spi.PolicyDecisionPort;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.bind.*;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;
@AutoConfiguration(before={ResourceServerAutoConfiguration.class,TerminalOwnerPolicyAutoConfiguration.class})
@ConditionalOnProperty(name="firefly.terminal.managed-posting.enabled",havingValue="true")
@Profile("!openapi-gen")
@Import(StaffPostingPolicyAutoConfiguration.DefaultPolicyBackend.class)
public class StaffPostingPolicyAutoConfiguration {
    @ConditionalOnMissingBean(PolicyDecisionPort.class)
    static class DefaultPolicyBackend {
        @Bean PolicyDecisionPort staffPostingCurrentPolicy(Environment environment){
            var binder=Binder.get(environment);var value=binder.bind("firefly.terminal.managed-posting.policy",Bindable.of(TerminalOwnerPolicyProperties.class)).orElseThrow(()->new IllegalStateException("Explicit current terminal policy configuration required"));
            binder.bind("firefly.terminal.bank-owner.policy",Bindable.of(TerminalOwnerPolicyProperties.class)).ifBound(legacy->{if(!legacy.equals(value))throw new IllegalStateException("One explicit current terminal policy backend required");});
            return new OpaPolicyDecisionAdapter(WebClient.builder().baseUrl(value.baseUri().toASCIIString()).codecs(c->{c.defaultCodecs().maxInMemorySize(16384);c.defaultCodecs().enableLoggingRequestDetails(false);}).build(),value.decisionPath());
        }
    }
}
