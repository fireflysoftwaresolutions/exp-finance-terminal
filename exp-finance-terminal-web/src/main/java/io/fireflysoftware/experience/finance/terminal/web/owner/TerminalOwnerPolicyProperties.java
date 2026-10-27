/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.web.owner;
import java.net.URI;import java.util.Set;
/** Explicit current policy endpoint, bound only inside the enabled default backend. */
public record TerminalOwnerPolicyProperties(URI baseUri,String decisionPath){
 public TerminalOwnerPolicyProperties{if(baseUri==null||baseUri.toString().length()>2048||baseUri.getHost()==null||!("https".equals(baseUri.getScheme())||"http".equals(baseUri.getScheme())&&Set.of("127.0.0.1","::1","[::1]").contains(baseUri.getHost()))||baseUri.getUserInfo()!=null||baseUri.getRawQuery()!=null||baseUri.getRawFragment()!=null||!(baseUri.getPath().isEmpty()||baseUri.getPath().equals("/"))||decisionPath==null||decisionPath.length()>512||!decisionPath.matches("[A-Za-z0-9_-]+(?:/[A-Za-z0-9_-]+)*"))throw new IllegalArgumentException("Explicit valid current-owner policy endpoint required");}
}
