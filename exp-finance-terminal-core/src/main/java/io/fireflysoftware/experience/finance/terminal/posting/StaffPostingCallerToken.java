/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.posting;
/** Request-local native token handle, excluded from command payloads and rendered diagnostics. */
public record StaffPostingCallerToken(String value){
    public StaffPostingCallerToken{if(value==null||value.length()>16384||!value.matches("[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+"))throw new IllegalArgumentException("Native bearer token required");}
    @Override public String toString(){return "StaffPostingCallerToken[redacted]";}
}
