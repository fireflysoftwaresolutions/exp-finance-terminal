/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.posting;
import java.util.*;
public record StaffPostingInvocation(String tenant,String book,UUID actionId,String expectedIntentDigest,String originalRequestJson,boolean recovery){
    public StaffPostingInvocation{if(tenant==null||book==null||actionId==null||actionId.equals(new UUID(0,0))||expectedIntentDigest==null||!expectedIntentDigest.matches("[0-9a-f]{64}")||originalRequestJson==null||originalRequestJson.isEmpty()||originalRequestJson.length()>1048576)throw new IllegalArgumentException("Complete bounded original staff posting required");}
    @Override public String toString(){return "StaffPostingInvocation[redacted]";}
}
