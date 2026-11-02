/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.posting;
import io.fireflysoftware.domain.accounting.sdk.model.GlPostingRequest;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(additionalProperties=Schema.AdditionalPropertiesValue.FALSE)
public record StaffPostingRequest(@Schema(requiredMode=Schema.RequiredMode.REQUIRED,pattern="[0-9a-f]{64}")String expectedIntentDigest,@Schema(requiredMode=Schema.RequiredMode.REQUIRED)GlPostingRequest posting){}
