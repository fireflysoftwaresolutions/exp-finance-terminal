/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.web.owner;
import io.fireflysoftware.experience.finance.terminal.owner.*;
import io.swagger.v3.oas.annotations.Operation;import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;import java.util.UUID;
@RestController
@ConditionalOnProperty(name="firefly.terminal.bank-owner.enabled",havingValue="true",matchIfMissing=false)
@SecurityRequirement(name="staffBearer")
@RequestMapping("/api/v1/staff/tenants/{tenantId}/banks/{bankId}/enrollments")
public final class BankOwnerDeskController{
 private final StaffBankOwnerReadService service;
 public BankOwnerDeskController(StaffBankOwnerReadService service){this.service=service;}
 @Operation(operationId="getCurrentStaffBankOwner",tags="Staff Bank Enrollment Owner",summary="Read the current Bank enrollment owner",description="Authenticated staff administrative observation through the organization domain. Current tenant-scoped policy is required at every layer. Native and effective observation deadlines are preserved; this is not book, product, currency or financial admission.")
 @GetMapping(value="/{enrollmentId}/versions/{version}/current",produces=MediaType.APPLICATION_JSON_VALUE)
 public Mono<ResponseEntity<StaffBankOwnerView>> current(@PathVariable("tenantId") UUID tenantId,@PathVariable("bankId") UUID bankId,@PathVariable("enrollmentId") UUID enrollmentId,@PathVariable("version") long version){
  if(version<=0)return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,"Positive exact enrollment version required"));
  return Mono.defer(()->service.current(new StaffBankOwnerQuery(tenantId,bankId,enrollmentId,version))).map(v->ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(v));
 }
}
