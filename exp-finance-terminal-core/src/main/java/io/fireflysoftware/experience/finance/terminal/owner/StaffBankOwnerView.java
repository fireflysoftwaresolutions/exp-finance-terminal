/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.owner;
import java.util.*;
/** Administrative current-owner summary; never a book, currency, or financial admission. */
public record StaffBankOwnerView(UUID tenantId, UUID bankId, UUID enrollmentId, long version,
 UUID institutionId, UUID issuerAuthorityId, Set<String> allowedUses, long controlVersion,
 long ownershipGeneration,long bankGeneration,long sourceGeneration,long currentUntilEpochMillis,long observationUntilEpochMillis) {
 public StaffBankOwnerView {Objects.requireNonNull(tenantId);Objects.requireNonNull(bankId);Objects.requireNonNull(enrollmentId);Objects.requireNonNull(institutionId);Objects.requireNonNull(issuerAuthorityId);allowedUses=Set.copyOf(allowedUses);if(version<=0||controlVersion<0||ownershipGeneration<0||bankGeneration<0||sourceGeneration<0||currentUntilEpochMillis<=0||observationUntilEpochMillis<=0||observationUntilEpochMillis>currentUntilEpochMillis||allowedUses.isEmpty())throw new IllegalArgumentException("Complete current owner summary required");}
 public StaffBankOwnerView withObservationUntil(long until){return new StaffBankOwnerView(tenantId,bankId,enrollmentId,version,institutionId,issuerAuthorityId,allowedUses,controlVersion,ownershipGeneration,bankGeneration,sourceGeneration,currentUntilEpochMillis,Math.min(observationUntilEpochMillis,until));}
 public boolean matches(StaffBankOwnerQuery q){return tenantId.equals(q.tenantId())&&bankId.equals(q.bankId())&&enrollmentId.equals(q.enrollmentId())&&version==q.version();}
}
