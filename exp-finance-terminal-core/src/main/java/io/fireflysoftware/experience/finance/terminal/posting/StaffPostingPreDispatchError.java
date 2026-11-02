/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.posting;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(additionalProperties=Schema.AdditionalPropertiesValue.FALSE)
public record StaffPostingPreDispatchError(int status,@Schema(allowableValues="STAFF_POSTING_PRE_DISPATCH_REJECTED")String code,@Schema(allowableValues="BEFORE_DOMAIN_DISPATCH")String phase){}
