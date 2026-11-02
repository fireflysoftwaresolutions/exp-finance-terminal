/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal;

import java.util.Objects;

/** A legacy transport reply is not a governed owner action or recovery receipt. */
public final class RecordOutcomeUnknownException extends RuntimeException {
    private final RecordCommand originalRequest;
    private final String transportRequestKey;

    public RecordOutcomeUnknownException(RecordCommand originalRequest, String transportRequestKey) {
        super("Original owner record outcome is unknown");
        this.originalRequest = Objects.requireNonNull(originalRequest);
        this.transportRequestKey = Objects.requireNonNull(transportRequestKey);
    }

    public RecordCommand originalRequest() { return originalRequest; }
    public String transportRequestKey() { return transportRequestKey; }
}
