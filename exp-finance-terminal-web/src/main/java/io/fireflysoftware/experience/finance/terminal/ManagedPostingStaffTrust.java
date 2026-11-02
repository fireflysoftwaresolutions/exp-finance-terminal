/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal;

import java.time.*;
import java.util.*;
import org.fireflyframework.security.api.domain.SecurityPrincipal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.jwt.Jwt;

/** Native namespace admission for two explicit posting routes; configuration is not financial authority. */
@ConfigurationProperties("firefly.terminal.managed-posting.staff")
public record ManagedPostingStaffTrust(boolean enabled,String issuer,String audience,String clientId,String platformTenant,
    String tenantClaim,String clientClaim,String kindClaim,String staffClaim,String scopeClaim,UUID resourceTenantId,UUID bookId) {
  public ManagedPostingStaffTrust {
    if(enabled) {
      for(String value:List.of(Objects.requireNonNull(issuer),Objects.requireNonNull(audience),Objects.requireNonNull(clientId),Objects.requireNonNull(platformTenant)))
        if(!text(value,512))throw new IllegalArgumentException("Explicit managed posting staff trust required");
      var names=Arrays.asList(tenantClaim,clientClaim,kindClaim,staffClaim,scopeClaim);
      if(names.stream().anyMatch(name->name==null||!name.matches("[A-Za-z_][A-Za-z0-9_]{0,63}"))||new HashSet<>(names).size()!=5
          ||names.stream().anyMatch(name->Set.of("iss","sub","aud","exp","nbf","iat").contains(name))
          ||resourceTenantId==null||bookId==null||resourceTenantId.equals(new UUID(0,0))||bookId.equals(new UUID(0,0)))
        throw new IllegalArgumentException("Explicit distinct native claims and resource route required");
    }
  }
  static ManagedPostingStaffTrust disabled(){return new ManagedPostingStaffTrust(false,null,null,null,null,null,null,null,null,null,null,null);}
  boolean candidate(Jwt jwt){return enabled&&platformTenant.equals(jwt.getClaims().get(tenantClaim));}
  boolean route(String method,String path){return enabled&&"POST".equals(method)&&(base().equals(path)||(base()+"/recovery").equals(path));}
  String base(){return "/api/v1/staff/tenants/"+resourceTenantId+"/books/"+bookId+"/ledger-journals";}
  String requiredScope(String path){return path.equals(base()+"/recovery")?"accounting:gl:recover":"accounting:gl:post";}
  boolean valid(Jwt jwt,Clock clock){return enabled&&issuer.equals(jwt.getIssuer()==null?null:jwt.getIssuer().toString())&&valid(jwt.getSubject(),jwt.getClaims(),clock);}
  boolean valid(SecurityPrincipal principal,Clock clock){return enabled&&principal!=null&&issuer.equals(principal.issuer())&&platformTenant.equals(principal.tenantId())&&valid(principal.subject(),principal.claims(),clock);}
  private boolean valid(String subject,Map<String,Object> claims,Clock clock){
    if(!text(subject,255)||!List.of(audience).equals(claims.get("aud"))
        ||!platformTenant.equals(claims.get(tenantClaim))||!clientId.equals(claims.get(clientClaim))||!"STAFF".equals(claims.get(kindClaim))
        ||!(claims.get("exp") instanceof Instant expiry)||!(claims.get("nbf") instanceof Instant start)||!expiry.isAfter(clock.instant())
        ||expiry.isAfter(Instant.parse("9999-12-31T23:59:59Z"))||clock.instant().isBefore(start)||!start.isBefore(expiry))return false;
    try {var actor=TerminalAuthentication.uuid(claims.get(staffClaim));if(new UUID(0,0).toString().equals(actor))return false;}catch(RuntimeException invalid){return false;}
    if(claims.containsKey("tenant_id")&&!tenantClaim.equals("tenant_id")&&!platformTenant.equals(claims.get("tenant_id")))return false;
    if(claims.containsKey("staff_actor_id")&&!staffClaim.equals("staff_actor_id")&&!claims.get(staffClaim).equals(claims.get("staff_actor_id")))return false;
    return true;
  }
  boolean scoped(SecurityPrincipal principal,String path){
    Object raw=principal.claims().get(scopeClaim);if(!(raw instanceof String text)||!text(text,4096)||principal.claims().containsKey("scp"))return false;
    var tokens=text.split(" ",-1);var scopes=new HashSet<>(Arrays.asList(tokens));
    return !scopes.contains("")&&scopes.size()==tokens.length&&scopes.contains(requiredScope(path))&&principal.scopes().contains(requiredScope(path));
  }
  private static boolean text(String value,int limit){return value!=null&&!value.isBlank()&&value.length()<=limit&&!value.codePoints().anyMatch(c->Character.isISOControl(c)||c>=0xd800&&c<=0xdfff||c==0xfffd);}
  @Override public String toString(){return "ManagedPostingStaffTrust[configured="+enabled+"]";}
}
