/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.web.posting.impl;
import io.fireflysoftware.experience.finance.terminal.posting.*;
import io.fireflysoftware.experience.finance.terminal.web.posting.StaffLedgerPostingsController;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice(assignableTypes=StaffLedgerPostingsController.class)
public class StaffPostingExceptionHandlerImpl {
    @ExceptionHandler(StaffPostingPreDispatchException.class)
    public ResponseEntity<StaffPostingPreDispatchError> beforeDomain(StaffPostingPreDispatchException error){return ResponseEntity.status(error.getStatusCode()).cacheControl(CacheControl.noStore()).contentType(MediaType.APPLICATION_JSON).body(new StaffPostingPreDispatchError(error.getStatusCode().value(),"STAFF_POSTING_PRE_DISPATCH_REJECTED","BEFORE_DOMAIN_DISPATCH"));}
}
