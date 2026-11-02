/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.posting;
import java.time.Instant;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(additionalProperties=Schema.AdditionalPropertiesValue.FALSE)
public record StaffPostingReceipt(UUID receiptId,UUID actionId,UUID postingId,long journalSequence,String intentDigest,boolean recovered,@Schema(ref="#/components/schemas/FireflyInstant",requiredMode=Schema.RequiredMode.REQUIRED) Instant recordedAt){}
