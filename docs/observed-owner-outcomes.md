# Observed owner outcomes

The real Domain record adapter returns observed owner identity, status, and fields. It does not substitute requested statuses, employee IDs, customer IDs, or notes for values the owner did not return.

## Closure and cancellation

The Accounts and Cards Domain close/cancel operations return no record body. After a successful write, the adapter reads the current record through that same genuine Domain SDK. A matching returned record determines the result; the requested close label does not. A missing record, another record ID, an unavailable read, or a refused read cannot confirm closure.

A known write refusal stays a refusal. A failure after dispatch or during the follow-up read preserves an unknown write outcome and the exact original request and transport key in `RecordOutcomeUnknownException`. No automatic mutation retry is performed. This legacy transport key is not a durable governed owner action or a read-only recovery contract. The current general HTTP error handling does not publish the exception's internal fields as a complete recovery API.

## Loans and record mutations

Loan rows expose the genuine application status UUID returned by the current Domain read. No `APPROVED` or `REJECTED` business label is inferred from a command or an untyped legacy decision response. A valid reply remains usable; an empty or otherwise insufficient decision response cannot prove completion. A typed authorized status catalog projection and complete decision semantics remain pending.

Other supported mutations likewise return owner fields. Draft removal requires an actually observed draft state; missing status never authorizes deletion. Invalid decision input cannot silently become a rejection. A missing masked card reference is not replaced with a full card number.

## Validation and limits

Eighteen focused tests exercise the real adapter with nine original generated Domain SDKs and controlled HTTP responses, including unknown outcomes, matching identity, actual state, incomplete replies, refusals, and unchanged existing scenarios. A separate full source run passes all 183 Financial Terminal cases and compiles its actual wiring; the combined reactor passes 209 final cases plus two original owner-producer cases.

These are source and controlled HTTP results. They do not establish complete account closure, card cancellation/activation, loan decisions, actual STAFF delegation, durable legacy audit, or all Experience → Domain → Core journeys. Existing default in-memory business/audit implementations remain architectural debt and are not qualified by the real Domain adapter tests. The separate six-service monetary integration is still open.
