/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.owner;
import reactor.core.publisher.Mono;
public interface StaffBankOwnerReadService {Mono<StaffBankOwnerView> current(StaffBankOwnerQuery query);}
