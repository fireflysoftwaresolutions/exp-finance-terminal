/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.posting;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;
/** This invocation has not dispatched a Domain call; prior attempts are not characterized. */
public final class StaffPostingPreDispatchException extends ResponseStatusException {
    public StaffPostingPreDispatchException(HttpStatusCode status){super(status,"Managed posting rejected before Domain dispatch; retain any prior original action");}
}
