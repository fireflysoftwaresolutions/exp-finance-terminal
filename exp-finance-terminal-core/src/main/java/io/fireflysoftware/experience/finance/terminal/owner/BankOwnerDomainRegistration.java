/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.owner;
import java.net.URI;import java.util.*;
/** Exact transport registration, never current caller or financial authority. */
public record BankOwnerDomainRegistration(UUID tenantId,UUID bankId,String registrationId,URI baseUri,URI tokenUri,URI jwksUri,String issuer,String audience,String subject,String clientId,String machineTenantId){
 public BankOwnerDomainRegistration{Objects.requireNonNull(tenantId);Objects.requireNonNull(bankId);UUID.fromString(machineTenantId);for(String t:new String[]{registrationId,issuer,audience,subject,clientId,machineTenantId})if(t==null||t.isBlank()||t.length()>500||t.chars().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Complete machine registration required");for(URI u:new URI[]{baseUri,tokenUri,jwksUri,URI.create(issuer)})if(u==null||!"https".equals(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null||u.getQuery()!=null||u.getFragment()!=null)throw new IllegalArgumentException("HTTPS endpoints required");if(!baseUri.getPath().isEmpty()&&!baseUri.getPath().equals("/"))throw new IllegalArgumentException("Domain root required");}
}
