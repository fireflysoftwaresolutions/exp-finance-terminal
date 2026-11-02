/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal;
import static org.junit.jupiter.api.Assertions.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.fireflyframework.security.api.domain.*;
import org.fireflyframework.security.core.context.PrincipalFactory;
import org.fireflyframework.security.spi.SecurityContextPort;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
/** Genuine native receiver/JWKS/current-policy HTTP probe; no SDK, financial owner or money effect acceptance. */
@ActiveProfiles("managed-posting-http-probe")
@SpringBootTest(classes={TerminalBoundaryHttpTest.Application.class,TerminalManagedPostingStaffHttpTest.Probe.class,TerminalSessionAuthorityHttpTest.SessionWriter.class},
 webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"server.address=127.0.0.1","firefly.eda.enabled=false","firefly.stepevents.enabled=false",
 "firefly.terminal.managed-posting.staff.enabled=true","firefly.terminal.managed-posting.staff.platform-tenant=opaque-bank-east",
 "firefly.terminal.managed-posting.staff.audience=staff-finance","firefly.terminal.managed-posting.staff.client-id=finance-browser",
 "firefly.terminal.managed-posting.staff.tenant-claim=tenant","firefly.terminal.managed-posting.staff.client-claim=azp",
 "firefly.terminal.managed-posting.staff.kind-claim=principal_kind","firefly.terminal.managed-posting.staff.staff-claim=staff_actor_id",
 "firefly.terminal.managed-posting.staff.scope-claim=scope","firefly.terminal.managed-posting.staff.resource-tenant-id=00000000-0000-0000-0000-000000000001",
 "firefly.terminal.managed-posting.staff.book-id=00000000-0000-0000-0000-000000000002"})
class TerminalManagedPostingStaffHttpTest {
 static final String BASE="/api/v1/staff/tenants/00000000-0000-0000-0000-000000000001/books/00000000-0000-0000-0000-000000000002/ledger-journals";
 static final AtomicInteger PROBES=new AtomicInteger();
 @LocalServerPort int port;
 @Autowired PrincipalFactory principals;
 @Autowired ReactiveJwtDecoder decoder;
 @Autowired ManagedPostingStaffTrust trust;
 @DynamicPropertySource static void properties(DynamicPropertyRegistry p){TerminalBoundaryHttpTest.trust(p);p.add("firefly.terminal.managed-posting.staff.issuer",()->TerminalBoundaryHttpTest.ISSUER);}
 @AfterAll static void stop(){TerminalBoundaryHttpTest.stop();}
 @BeforeEach void reset(){new TerminalBoundaryHttpTest().reset();PROBES.set(0);}
 @RestController @Profile("managed-posting-http-probe") static class Probe {
  private final SecurityContextPort contexts;Probe(SecurityContextPort contexts){this.contexts=contexts;}
  @RequestMapping(value={BASE,BASE+"/recovery",BASE+"/extra",BASE+"/", "/api/v1/staff/managed-probe"})
  Mono<Map<String,Object>> probe(){return contexts.currentPrincipal().map(principal->{PROBES.incrementAndGet();return Map.of("subject",principal.subject(),"issuer",principal.issuer(),"tenant",principal.tenantId(),"claims",principal.claims());});}
 }
 String token(Map<String,Object> changes)throws Exception{
  var now=Instant.now();var values=new LinkedHashMap<String,Object>();values.put("iss",TerminalBoundaryHttpTest.ISSUER);values.put("aud",List.of("staff-finance"));values.put("sub","original-staff-subject");
  values.put("exp",Date.from(now.plusSeconds(120)));values.put("nbf",Date.from(now.minusSeconds(1)));values.put("tenant","opaque-bank-east");values.put("azp","finance-browser");values.put("principal_kind","STAFF");values.put("staff_actor_id",TerminalBoundaryHttpTest.ACTOR.toString());values.put("scope","accounting:gl:post accounting:gl:recover");
  changes.forEach((k,v)->{if("missing".equals(v))values.remove(k);else values.put(k,v);});var builder=new JWTClaimsSet.Builder();values.forEach(builder::claim);
  var jwt=new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(TerminalBoundaryHttpTest.KEY.getKeyID()).build(),builder.build());jwt.sign(new RSASSASigner(TerminalBoundaryHttpTest.KEY));return jwt.serialize();
 }
 java.net.http.HttpResponse<String> call(String method,String path,String token)throws Exception{var helper=new TerminalBoundaryHttpTest();helper.port=port;return helper.signed(method,path,"{}",token,null);}
 SecurityPrincipal observed(){return (SecurityPrincipal)TerminalBoundaryHttpTest.POLICY_CALLS.getFirst().get("principal");}
 @Test void nativeOpaquePostPreservesOriginalIssuerSubjectClientActorAndClaims()throws Exception{
  var raw=token(Map.of());var decoded=decoder.decode(raw).block();var response=call("POST",BASE,raw);assertEquals(200,response.statusCode(),response.body());assertEquals(1,PROBES.get());
  var actual=observed();assertEquals(decoded.getClaims(),actual.claims());assertEquals("opaque-bank-east",actual.tenantId());assertEquals("original-staff-subject",actual.subject());assertEquals(TerminalBoundaryHttpTest.ISSUER,actual.issuer());assertEquals("finance-browser",actual.claims().get("azp"));assertEquals(TerminalBoundaryHttpTest.ACTOR.toString(),actual.attributes().get("staffActorId"));
  var context=(Map<?,?>)TerminalBoundaryHttpTest.POLICY_CALLS.getFirst().get("context");assertEquals("opaque-bank-east",context.get("tenantId"));assertEquals(BASE,context.get("path"));assertEquals("terminal.finance.manage",TerminalBoundaryHttpTest.POLICY_CALLS.getFirst().get("action"));
 }
 @Test void recoveryRequiresItsOwnNativeScope()throws Exception{assertEquals(200,call("POST",BASE+"/recovery",token(Map.of("scope","accounting:gl:recover"))).statusCode());assertEquals(403,call("POST",BASE,token(Map.of("scope","accounting:gl:recover"))).statusCode());assertEquals(403,call("POST",BASE+"/recovery",token(Map.of("scope","accounting:gl:post"))).statusCode());assertEquals(1,PROBES.get());}
 @Test void opaqueTenantCannotReachLegacyOrAdjacentOrNonPostRoutes()throws Exception{for(var path:List.of(BASE+"/extra",BASE+"/",BASE.replace("000000000002","000000000099"),BASE+"/recovery/","/api/v1/staff/managed-probe","/api/v1/staff/masters/CURRENCY"))assertEquals(403,call("POST",path,token(Map.of())).statusCode(),path);assertEquals(403,call("GET",BASE,token(Map.of())).statusCode());assertEquals(0,PROBES.get());assertTrue(TerminalBoundaryHttpTest.POLICY_CALLS.isEmpty());assertEquals(0,TerminalBoundaryHttpTest.IDENTITIES.get());}
 @Test void wrongNativeNamespaceClientKindActorAndValidityCannotReachCurrentPolicy()throws Exception{
  for(var change:List.<Map<String,Object>>of(Map.of("tenant","other-opaque"),Map.of("azp","another-client"),Map.of("principal_kind","MACHINE"),Map.of("staff_actor_id",new UUID(0,0).toString()),Map.of("staff_actor_id","not-an-actor"),Map.of("nbf",Date.from(Instant.now().plusSeconds(120))),Map.of("nbf","missing"),Map.of("tenant_id",TerminalBoundaryHttpTest.TENANT.toString())))assertEquals(401,call("POST",BASE,token(change)).statusCode(),change.keySet().toString());
  assertEquals(0,PROBES.get());assertTrue(TerminalBoundaryHttpTest.POLICY_CALLS.isEmpty());
 }
 @Test void wrongIssuerAudienceAndSignatureRemainNativeDecoderFailures()throws Exception{
  for(var change:List.<Map<String,Object>>of(Map.of("iss","https://other.invalid"),Map.of("aud",List.of("other")),Map.of("exp",Date.from(Instant.now().minusSeconds(120)))))assertEquals(401,call("POST",BASE,token(change)).statusCode());
  var original=SignedJWT.parse(token(Map.of()));var key=new com.nimbusds.jose.jwk.gen.RSAKeyGenerator(2048).keyID(TerminalBoundaryHttpTest.KEY.getKeyID()).generate();var forged=new SignedJWT(original.getHeader(),original.getJWTClaimsSet());forged.sign(new RSASSASigner(key));assertEquals(401,call("POST",BASE,forged.serialize()).statusCode());assertEquals(0,PROBES.get());
 }
 @Test void currentWithdrawalCannotReuseEarlierPermit()throws Exception{var raw=token(Map.of());assertEquals(200,call("POST",BASE,raw).statusCode());TerminalBoundaryHttpTest.DECISION.set(Decision.deny("current synthetic withdrawal"));assertEquals(403,call("POST",BASE,raw).statusCode());assertEquals(1,PROBES.get());assertEquals(2,TerminalBoundaryHttpTest.POLICY_CALLS.size());}
 @Test void emptyOrObligationBearingCurrentDecisionsRefuseBeforeController()throws Exception{for(var value:Arrays.asList(null,Decision.permit(List.of(new Obligation("mask",Map.of()))),Decision.indeterminate("offline"))){TerminalBoundaryHttpTest.DECISION.set(value);assertEquals(403,call("POST",BASE,token(Map.of())).statusCode());}assertEquals(0,PROBES.get());}
 @Test void legacyConstructorStillRejectsOpaqueTenantAndStillProjectsLegacyUuidExactly()throws Exception{var legacy=new TerminalAuthentication(principals,Clock.systemUTC());assertThrows(RuntimeException.class,()->legacy.convert(decoder.decode(token(Map.of())).block()).block());var decoded=decoder.decode(new TerminalBoundaryHttpTest().token(Map.of())).block();var projected=legacy.convert(decoded).block().getPrincipal();assertInstanceOf(SecurityPrincipal.class,projected);var principal=(SecurityPrincipal)projected;assertEquals(TerminalBoundaryHttpTest.TENANT.toString(),principal.tenantId());assertEquals(decoded.getClaims(),principal.claims());assertFalse(ManagedPostingStaffTrust.disabled().route("POST",BASE));}
 @Test void retainedSessionCannotSubstituteForBearerOrDisableCsrf()throws Exception{var session=new TerminalSessionAuthorityHttpTest();session.port=port;var cookie=session.verifiedSession();assertEquals(403,session.send("POST",BASE,"{}","Cookie",cookie).statusCode());assertEquals(401,session.send("POST",BASE,"{}","Cookie",cookie,"Authorization","Bearer invalid-terminal-token").statusCode());assertEquals(0,PROBES.get());assertTrue(TerminalBoundaryHttpTest.POLICY_CALLS.isEmpty());assertEquals(200,session.send("POST",BASE,"{}","Cookie",cookie,"Authorization","Bearer "+token(Map.of())).statusCode());assertEquals(1,PROBES.get());}
 @Test void legacyUuidTokenCannotBorrowTheManagedOpaqueRoute()throws Exception{assertEquals(403,call("POST",BASE,new TerminalBoundaryHttpTest().token(Map.of())).statusCode());assertEquals(0,PROBES.get());assertTrue(TerminalBoundaryHttpTest.POLICY_CALLS.isEmpty());}
}
