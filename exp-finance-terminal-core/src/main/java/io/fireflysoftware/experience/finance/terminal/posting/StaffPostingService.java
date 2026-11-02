/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.posting;
import reactor.core.publisher.Mono;
/** Submit or recover the same original action through the Accounting domain only. */
public interface StaffPostingService {Mono<StaffPostingResponse> submit(String tenant,String book,byte[] completeRequest,boolean recovery);}
