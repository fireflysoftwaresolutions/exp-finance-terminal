/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.posting;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;
/** A refusal characterizes this invocation; it does not close unknown effects of an earlier original action. */
@Schema(additionalProperties=Schema.AdditionalPropertiesValue.FALSE)
public record StaffPostingResponse(UUID actionId,String intentDigest,Status status,Integer domainStatus,Integer ownerStatus,StaffPostingReceipt receipt){
    public enum Status {COMMITTED,OUTCOME_UNKNOWN,COMMITTED_RESPONSE_UNAVAILABLE,CURRENT_INVOCATION_REFUSED,NOT_OBSERVED,DOMAIN_PRE_DISPATCH_REFUSED}
}
