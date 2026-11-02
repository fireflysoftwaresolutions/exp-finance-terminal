/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.owner;
import reactor.core.publisher.Mono;
import org.fireflyframework.core.transaction.R2dbcOperationBudget;
import java.util.Objects;
public interface OrganizationBankOwnerPort {
 Mono<Observation> read(StaffBankOwnerQuery query,R2dbcOperationBudget original,java.time.Instant originalUntil);
 record Observation(StaffBankOwnerView view,R2dbcOperationBudget window){public Observation{Objects.requireNonNull(view);Objects.requireNonNull(window);}}
}
