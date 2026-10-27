package io.fireflysoftware.experience.finance.terminal.owner;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import org.springframework.boot.test.context.runner.ApplicationContextRunner;import org.springframework.context.annotation.*;import org.fireflyframework.security.spi.*;import org.fireflyframework.security.api.domain.Decision;import org.springframework.security.oauth2.client.*;import org.springframework.security.oauth2.client.registration.*;import reactor.core.publisher.Mono;
class StaffBankOwnerConfigurationTest{
 static class Dependencies{@Bean SecurityContextPort context(){return Mono::empty;}@Bean PolicyDecisionPort policy(){return(p,a,r,c)->Mono.just(Decision.deny("Source test only"));}@Bean ReactiveOAuth2AuthorizedClientManager clients(){return r->Mono.empty();}@Bean ReactiveClientRegistrationRepository registrations(){return id->Mono.empty();}}
 ApplicationContextRunner runner(){return new ApplicationContextRunner().withUserConfiguration(StaffBankOwnerConfiguration.class,Dependencies.class);}
 @Test void defaultOffDoesNotBindAnUnusedMachine(){runner().withPropertyValues("firefly.terminal.bank-owner.machine.base-uri=invalid").run(c->{assertNull(c.getStartupFailure());assertTrue(c.getBeansOfType(StaffBankOwnerReadService.class).isEmpty());});}
 @Test void enabledWithoutStrictSignedStaffTrustFailsClosed(){runner().withPropertyValues("firefly.terminal.bank-owner.enabled=true").run(c->{assertNotNull(c.getStartupFailure());});}
}
