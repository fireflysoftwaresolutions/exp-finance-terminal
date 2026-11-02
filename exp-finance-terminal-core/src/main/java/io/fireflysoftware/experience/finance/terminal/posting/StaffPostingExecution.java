/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.posting;
import reactor.core.publisher.Mono;
public interface StaffPostingExecution {Mono<StaffPostingResponse> execute(StaffPostingInvocation original);}
