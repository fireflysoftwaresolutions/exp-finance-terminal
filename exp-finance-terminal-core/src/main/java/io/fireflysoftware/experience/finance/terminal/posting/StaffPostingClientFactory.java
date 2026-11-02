/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.posting;
import io.fireflysoftware.domain.accounting.sdk.api.ManagedLedgerJournalsApi;
import java.time.Duration;
import reactor.core.publisher.Mono;
/** A fresh original-caller-bound generated owner client; no implicit machine grant or authority fallback. */
public interface StaffPostingClientFactory {
    Mono<Client> current(String tenantId,String bookId,boolean recovery);
    interface Client {ManagedLedgerJournalsApi api();Duration remaining();Mono<Void> revalidate();}
}
