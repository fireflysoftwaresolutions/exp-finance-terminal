# Financial Staff Terminal: source reference

[Repository guide](../../README.md) · [Machine-readable inventory](source-inventory.json)

This reference is derived from checked-in source. It separates declared HTTP mappings, typed contracts, property placeholders, and validation locations. Java reflection, inherited framework routes, generated targets, and runtime infrastructure are outside this static inventory.

## HTTP operations

| Mapping | Method | Controller | Kind |
| --- | --- | --- | --- |
| `GET /api/v1/staff/records/{area}` | `public Flux<StaffRecord> list(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId, @PathVariable String area)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `GET /api/v1/staff/records/{area}/{recordId}` | `public Mono<StaffRecord> open(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId, @PathVariable String area, @PathVariable UUID recordId)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `GET /api/v1/staff/distributors` | `public Flux<DistributorView> distributors(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `GET /api/v1/staff/configuration/{key}` | `public Mono<String> getProperty(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId, @PathVariable String key)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `GET /api/v1/staff/masters/{type}` | `public Flux<MasterRecord> masters(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId, @PathVariable String type)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `GET /api/v1/staff/accounts/{accountId}/servicing-mandates/{mandateId}` | `public Mono<InstructionPort.Mandate> mandate(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId, @PathVariable UUID accountId, @PathVariable UUID mandateId)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `GET /api/v1/staff/standing-orders/{runId}` | `public Mono<InstructionPort.StandingOrder> standingOrder(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId, @PathVariable UUID runId)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `GET /api/v1/staff/direct-debits/{mandateId}` | `public Mono<InstructionPort.DirectDebit> directDebit(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId, @PathVariable UUID mandateId)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `POST /api/v1/staff/sessions` | `public Mono<StaffSession> signIn( @Parameter(hidden = true) @AuthenticationPrincipal SecurityPrincipal principal, @RequestBody SignInRequest body)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `POST /api/v1/staff/records/{area}/{recordId}/close` | `public Mono<StaffRecord> close(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId, @PathVariable String area, @PathVariable UUID recordId, @RequestBody RecordBody body)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `POST /api/v1/staff/records/{area}/{recordId}/remove` | `public Mono<Void> removeDraft(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId, @PathVariable String area, @PathVariable UUID recordId, @RequestBody RecordBody body)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `POST /api/v1/staff/records/{area}/{recordId}/decide` | `public Mono<StaffRecord> decide(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId, @PathVariable String area, @PathVariable UUID recordId, @RequestBody RecordBody body)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `POST /api/v1/staff/records/{area}/{recordId}/distributor` | `public Mono<StaffRecord> attach(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId, @PathVariable String area, @PathVariable UUID recordId, @RequestBody RecordBody body)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `PUT /api/v1/staff/records/{area}/{recordId}` | `public Mono<StaffRecord> update(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId, @PathVariable String area, @PathVariable UUID recordId, @RequestBody RecordBody body)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |
| `PUT /api/v1/staff/configuration/{key}` | `public Mono<String> putProperty(@RequestHeader(value = "X-Actor-Id", required = false) UUID actorId, @PathVariable String key, @RequestBody PropertyBody body)` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) | Local declaration |

## Data contracts

### AccessCheck

[AccessCheck](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/AccessCheck.java) · DTO / record / model

| Field | Java type | Validation annotations |
| --- | --- | --- |
| `actorId` | `UUID` | See declaration / service rules |
| `action` | `String` | See declaration / service rules |
| `resourceType` | `String` | See declaration / service rules |
| `resourceId` | `String` | See declaration / service rules |

### AccessOutcome

[AccessOutcome](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/AccessOutcome.java) · DTO / record / model

| Field | Java type | Validation annotations |
| --- | --- | --- |
| `allowed` | `boolean` | See declaration / service rules |
| `reasonCode` | `String` | See declaration / service rules |

### AuditEntry

[AuditEntry](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/AuditEntry.java) · DTO / record / model

| Field | Java type | Validation annotations |
| --- | --- | --- |
| `auditId` | `UUID` | See declaration / service rules |
| `employeeId` | `UUID` | See declaration / service rules |
| `customerPartyId` | `UUID` | See declaration / service rules |
| `reason` | `String` | See declaration / service rules |
| `action` | `String` | See declaration / service rules |
| `area` | `String` | See declaration / service rules |
| `recordId` | `UUID` | See declaration / service rules |

### DistributorLink

[DistributorLink](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DistributorLink.java) · DTO / record / model

| Field | Java type | Validation annotations |
| --- | --- | --- |
| `distributorId` | `UUID` | See declaration / service rules |
| `recordId` | `UUID` | See declaration / service rules |
| `customerPartyId` | `UUID` | See declaration / service rules |
| `employeeId` | `UUID` | See declaration / service rules |
| `channel` | `String` | See declaration / service rules |
| `collaboratorId` | `UUID` | See declaration / service rules |
| `area` | `String` | See declaration / service rules |

### DistributorView

[DistributorView](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DistributorView.java) · DTO / record / model

| Field | Java type | Validation annotations |
| --- | --- | --- |
| `distributorId` | `UUID` | See declaration / service rules |
| `name` | `String` | See declaration / service rules |
| `role` | `String` | See declaration / service rules |
| `partyId` | `UUID` | See declaration / service rules |
| `channel` | `String` | See declaration / service rules |
| `agencyId` | `UUID` | See declaration / service rules |
| `collaboratorId` | `UUID` | See declaration / service rules |

### MasterRecord

[MasterRecord](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/MasterRecord.java) · DTO / record / model

| Field | Java type | Validation annotations |
| --- | --- | --- |
| `type` | `String` | See declaration / service rules |
| `id` | `UUID` | See declaration / service rules |
| `code` | `String` | See declaration / service rules |
| `name` | `String` | See declaration / service rules |

### RecordCommand

[RecordCommand](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/RecordCommand.java) · DTO / record / model

| Field | Java type | Validation annotations |
| --- | --- | --- |
| `area` | `StaffArea` | See declaration / service rules |
| `recordId` | `UUID` | See declaration / service rules |
| `customerPartyId` | `UUID` | See declaration / service rules |
| `reason` | `String` | See declaration / service rules |
| `note` | `String` | See declaration / service rules |
| `distributorId` | `UUID` | See declaration / service rules |
| `collaboratorId` | `UUID` | See declaration / service rules |
| `channel` | `String` | See declaration / service rules |
| `actorId` | `UUID` | See declaration / service rules |
| `status` | `String` | See declaration / service rules |
| `decision` | `String` | See declaration / service rules |

### StaffRecord

[StaffRecord](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffRecord.java) · DTO / record / model

| Field | Java type | Validation annotations |
| --- | --- | --- |
| `id` | `UUID` | See declaration / service rules |
| `area` | `String` | See declaration / service rules |
| `customerPartyId` | `UUID` | See declaration / service rules |
| `actorId` | `UUID` | See declaration / service rules |
| `status` | `String` | See declaration / service rules |
| `reference` | `String` | See declaration / service rules |
| `distributorId` | `UUID` | See declaration / service rules |
| `note` | `String` | See declaration / service rules |

### StaffSession

[StaffSession](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffSession.java) · DTO / record / model

| Field | Java type | Validation annotations |
| --- | --- | --- |
| `actorId` | `UUID` | See declaration / service rules |
| `provider` | `String` | See declaration / service rules |

### SubjectRef

[SubjectRef](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/SubjectRef.java) · DTO / record / model

| Field | Java type | Validation annotations |
| --- | --- | --- |
| `actorId` | `UUID` | See declaration / service rules |
| `provider` | `String` | See declaration / service rules |

## Ports and service contracts

### AccessPort

[AccessPort](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/AccessPort.java)

```java
Mono<AccessOutcome> check(AccessCheck request);
```

### AuditPort

[AuditPort](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/AuditPort.java)

```java
Mono<AuditEntry> append(AuditEntry entry);
Flux<AuditEntry> list();
```

### ConfigurationPort

[ConfigurationPort](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/ConfigurationPort.java)

```java
Mono<String> getRequired(String key);
Mono<String> put(String key, String value, java.util.UUID employeeId);
Flux<MasterRecord> masters(String type);
```

### DistributorPort

[DistributorPort](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DistributorPort.java)

```java
Flux<DistributorView> list();
Mono<DistributorView> link(DistributorLink link);
```

### IdentityPort

[IdentityPort](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/IdentityPort.java)

```java
Mono<SubjectRef> authenticate(String username);
Mono<SubjectRef> resolve(String actorId);
```

### ImpersonationPort

[ImpersonationPort](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/ImpersonationPort.java)

```java
Mono<AuditEntry> impersonate(UUID employeeId, UUID customerPartyId, String reason, String action, String area, UUID recordId);
```

### InstructionPort

[InstructionPort](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/InstructionPort.java)

```java
Mono<Mandate> mandate(UUID accountId, UUID mandateId);
Mono<StandingOrder> standingOrder(UUID runId);
Mono<DirectDebit> directDebit(UUID mandateId);
record Mandate(UUID id, UUID accountId, UUID partyId, String role, boolean active);
record StandingOrder(UUID id, UUID paymentScheduleId, BigDecimal amount, String currency, LocalDate valueDate);
record DirectDebit(UUID id, UUID debtorAccountId, UUID creditorPartyId, String status, BigDecimal amountLimit, String currency, String reference);
```

### StaffRecordPort

[StaffRecordPort](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffRecordPort.java)

```java
Flux<StaffRecord> list(StaffArea area);
Mono<StaffRecord> get(StaffArea area, UUID recordId);
Mono<StaffRecord> update(RecordCommand command);
Mono<StaffRecord> close(RecordCommand command);
Mono<Void> removeDraft(RecordCommand command, String requiredStatus);
Mono<StaffRecord> decide(RecordCommand command);
```

## Configuration

Values below come from source placeholders. Plain YAML values and configuration-property classes must also be reviewed at the linked configuration sources. Secret fallback values are deliberately redacted.

| Key | Fallback | Source |
| --- | --- | --- |
| `api-configuration.domain-platform.security-center.base-path` | `http://localhost:8085` | [TerminalWiring](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalWiring.java) |
| `firefly.terminal.security.issuer` | Empty | [TerminalSecurityConfiguration](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalSecurityConfiguration.java) |
| `firefly.terminal.security.audience` | Empty | [TerminalSecurityConfiguration](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalSecurityConfiguration.java) |
| `firefly.terminal.security.jwk-set-uri` | Empty | [TerminalSecurityConfiguration](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalSecurityConfiguration.java) |
| `firefly.security.resource-server.strict-policy` | `false` | [TerminalSecurityConfiguration](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalSecurityConfiguration.java) |
| `SERVER_PORT` | `8120` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `SECURITY_CENTER_URL` | `http://localhost:8085` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `FIREFLY_TERMINAL_ADAPTER` | `sandbox` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `CONFIG_MGMT_URL` | `http://localhost:8088` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `MASTER_DATA_URL` | `http://localhost:8089` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `DISTRIBUTOR_MGMT_URL` | `http://localhost:8070` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `BANKING_ACCOUNTS_URL` | `http://localhost:8090` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `ENDPOINT_DOMAIN_PAYMENTS` | `http://localhost:8092` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `BANKING_CARDS_URL` | `http://localhost:8091` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `LOAN_ORIGINATION_URL` | `http://localhost:8092` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `MORTGAGE_URL` | `http://localhost:8093` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `SUPPLY_CHAIN_FINANCE_URL` | `http://localhost:8094` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `COLLATERAL_URL` | `http://localhost:8095` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `COLLECTIONS_URL` | `http://localhost:8097` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `PROVISIONING_URL` | `http://localhost:8098` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |
| `AML_URL` | `http://localhost:8099` | [application](../../exp-finance-terminal-web/src/main/resources/application.yaml) |

## Security and failure evidence

| Declaration or failure site | Source |
| --- | --- |
| `new IllegalStateException("PROPERTY_NOT_SET")));` | [DomainConfigurationClient](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DomainConfigurationClient.java) |
| `new TerminalDeniedException(exception.getMessage()));` | [DomainConfigurationClient](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DomainConfigurationClient.java) |
| `new TerminalDeniedException(exception.getMessage()));` | [DomainConfigurationClient](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DomainConfigurationClient.java) |
| `new IllegalStateException("Distributor audit log was not recorded"));` | [DomainDistributorClient](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DomainDistributorClient.java) |
| `new IllegalStateException("Distributor id was not recorded"));` | [DomainDistributorClient](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DomainDistributorClient.java) |
| `new TerminalDeniedException("REMOVAL_NOT_APPLICABLE"));` | [DomainRecordClient](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DomainRecordClient.java) |
| `new TerminalDeniedException("DRAFT_REQUIRED"));` | [DomainRecordClient](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DomainRecordClient.java) |
| `new TerminalDeniedException("DECISION_NOT_APPLICABLE"));` | [DomainRecordClient](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DomainRecordClient.java) |
| `new TerminalDeniedException("DECISION_NOT_APPLICABLE"));` | [DomainRecordClient](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DomainRecordClient.java) |
| `new TerminalDeniedException("UPDATE_NOT_APPLICABLE"));` | [DomainRecordClient](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DomainRecordClient.java) |
| `new TerminalDeniedException("CLOSE_NOT_APPLICABLE"));` | [DomainRecordClient](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DomainRecordClient.java) |
| `new TerminalDeniedException("REMOVAL_NOT_APPLICABLE"));` | [DomainRecordClient](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DomainRecordClient.java) |
| `new TerminalDeniedException("PROPERTY_NOT_SET");` | [DomainRecordClient](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/DomainRecordClient.java) |
| `new TerminalDeniedException("REQUEST_INCOMPLETE"));` | [ImpersonationCoordinator](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/ImpersonationCoordinator.java) |
| `new TerminalDeniedException("ACCESS_DECISION_MISSING")))` | [ImpersonationCoordinator](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/ImpersonationCoordinator.java) |
| `new TerminalDeniedException(outcome.reasonCode()));` | [ImpersonationCoordinator](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/ImpersonationCoordinator.java) |
| `new TerminalDeniedException("IMPERSONATION_AUDIT_NOT_CONFIRMED")));` | [ImpersonationCoordinator](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/ImpersonationCoordinator.java) |
| `new TerminalDeniedException("REQUEST_INCOMPLETE"));` | [InstructionDesk](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/InstructionDesk.java) |
| `new TerminalDeniedException("ACTOR_UNKNOWN")))` | [InstructionDesk](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/InstructionDesk.java) |
| `new TerminalDeniedException("ACTOR_MISMATCH")))` | [InstructionDesk](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/InstructionDesk.java) |
| `new TerminalDeniedException("ACCESS_DECISION_MISSING")))` | [InstructionDesk](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/InstructionDesk.java) |
| `new TerminalDeniedException(outcome.reasonCode())));` | [InstructionDesk](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/InstructionDesk.java) |
| `new TerminalDeniedException("PROPERTY_NOT_SET"));` | [SandboxConfigurationStore](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/SandboxConfigurationStore.java) |
| `new TerminalDeniedException("REQUEST_INCOMPLETE"));` | [SandboxConfigurationStore](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/SandboxConfigurationStore.java) |
| `new TerminalDeniedException("MASTER_TYPE_UNKNOWN"));` | [SandboxConfigurationStore](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/SandboxConfigurationStore.java) |
| `new TerminalDeniedException("DISTRIBUTOR_NOT_FOUND"));` | [SandboxDistributorDirectory](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/SandboxDistributorDirectory.java) |
| `new TerminalDeniedException("RECORD_NOT_FOUND"));` | [SandboxRecordBook](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/SandboxRecordBook.java) |
| `new TerminalDeniedException("PARTY_MISMATCH"));` | [SandboxRecordBook](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/SandboxRecordBook.java) |
| `new TerminalDeniedException("PARTY_MISMATCH"));` | [SandboxRecordBook](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/SandboxRecordBook.java) |
| `new TerminalDeniedException("REMOVAL_NOT_APPLICABLE"));` | [SandboxRecordBook](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/SandboxRecordBook.java) |
| `new TerminalDeniedException("PARTY_MISMATCH"));` | [SandboxRecordBook](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/SandboxRecordBook.java) |
| `new TerminalDeniedException("DRAFT_REQUIRED"));` | [SandboxRecordBook](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/SandboxRecordBook.java) |
| `new TerminalDeniedException("DECISION_NOT_APPLICABLE"));` | [SandboxRecordBook](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/SandboxRecordBook.java) |
| `new TerminalDeniedException("PARTY_MISMATCH"));` | [SandboxRecordBook](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/SandboxRecordBook.java) |
| `new TerminalDeniedException("ACTOR_UNKNOWN")))` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException("ACCESS_DECISION_MISSING")))` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException(outcome.reasonCode()))));` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException("REQUEST_INCOMPLETE"));` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException("ACTOR_UNKNOWN")))` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException("ACTOR_MISMATCH")))` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException("ACCESS_DECISION_MISSING")))` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException(outcome.reasonCode()))));` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException("REMOVAL_NOT_APPLICABLE"));` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException("DECISION_NOT_APPLICABLE"));` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException("IMPERSONATION_NOT_CONFIRMED"))))` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException("REQUEST_INCOMPLETE"));` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException("ACTOR_UNKNOWN")))` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException("ACTOR_MISMATCH")))` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException("ACCESS_DECISION_MISSING")))` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException(outcome.reasonCode())));` | [StaffTerminal](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffTerminal.java) |
| `new TerminalDeniedException("PROPERTY_NOT_SET");` | [TerminalWiring](../../exp-finance-terminal-core/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalWiring.java) |
| `@SecurityRequirement(name = "staffBearer")` | [StaffController](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/StaffController.java) |
| `HttpStatus.FORBIDDEN` | [TerminalAccessFilter](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalAccessFilter.java) |
| `new TerminalDeniedException("STAFF_STRICT_POLICY_REQUIRED"));` | [TerminalAccessFilter](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalAccessFilter.java) |
| `new TerminalDeniedException("STAFF_ACTOR_MISMATCH"));` | [TerminalAccessFilter](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalAccessFilter.java) |
| `new TerminalDeniedException("STAFF_ACTOR_MISMATCH"));` | [TerminalAccessFilter](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalAccessFilter.java) |
| `new TerminalDeniedException("STAFF_ACCESS_DENIED")))` | [TerminalAccessFilter](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalAccessFilter.java) |
| `new TerminalDeniedException("STAFF_ACCESS_DENIED"));` | [TerminalAccessFilter](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalAccessFilter.java) |
| `new OAuth2AuthenticationException(new OAuth2Error("invalid_token"),"Invalid staff authentication");` | [TerminalAuthentication](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalAuthentication.java) |
| `new BadJwtException("Staff trust is not configured"));` | [TerminalSecurityConfiguration](../../exp-finance-terminal-web/src/main/java/io/fireflysoftware/experience/finance/terminal/TerminalSecurityConfiguration.java) |

## Build and dependencies

### exp-finance-terminal-core

[exp-finance-terminal-core/pom.xml](../../exp-finance-terminal-core/pom.xml)

Coordinates: `io.fireflysoftware:exp-finance-terminal-core:26.10.6`. Parent: `io.fireflysoftware:exp-finance-terminal:26.10.6`.

- `com.fasterxml.jackson.core:jackson-databind:`
- `io.fireflysoftware:domain-banking-accounts-sdk:26.10.6`
- `io.fireflysoftware:domain-banking-cards-sdk:26.10.6`
- `io.fireflysoftware:domain-core-configuration-sdk:26.10.6`
- `io.fireflysoftware:domain-core-reference-data-sdk:26.10.6`
- `io.fireflysoftware:domain-core-security-center-core:26.10.6`
- `io.fireflysoftware:domain-core-security-center-sdk:26.10.6`
- `io.fireflysoftware:domain-distributor-branding-sdk:26.10.6`
- `io.fireflysoftware:domain-lending-collateral-management-sdk:26.10.6`
- `io.fireflysoftware:domain-lending-delinquency-collections-sdk:26.10.6`
- `io.fireflysoftware:domain-lending-loan-origination-sdk:26.10.6`
- `io.fireflysoftware:domain-lending-mortgage-sdk:26.10.6`
- `io.fireflysoftware:domain-lending-provisioning-risk-sdk:26.10.6`
- `io.fireflysoftware:domain-lending-regulatory-compliance-sdk:26.10.6`
- `io.fireflysoftware:domain-lending-supply-chain-finance-sdk:26.10.6`
- `io.fireflysoftware:domain-payments-sdk:26.10.6`
- `org.springframework.boot:spring-boot-starter-test:`
- `org.springframework.boot:spring-boot-starter-webflux:`

### exp-finance-terminal-sdk

[exp-finance-terminal-sdk/pom.xml](../../exp-finance-terminal-sdk/pom.xml)

Coordinates: `io.fireflysoftware:exp-finance-terminal-sdk:26.10.6`. Parent: `io.fireflysoftware:exp-finance-terminal:26.10.6`.

- `com.fasterxml.jackson.core:jackson-databind:`
- `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:`
- `com.fasterxml.jackson.datatype:jackson-datatype-jsr310:`
- `com.squareup.okhttp3:logging-interceptor:`
- `com.squareup.okhttp3:okhttp:`
- `io.fireflysoftware:exp-finance-terminal-web:`
- `io.swagger.core.v3:swagger-annotations:`
- `jakarta.annotation:jakarta.annotation-api:`
- `javax.annotation:javax.annotation-api:1.3.2`
- `org.openapitools:jackson-databind-nullable:0.2.8`
- `org.projectlombok:lombok:`
- `org.springframework.boot:spring-boot-starter-test:`
- `org.springframework:spring-webflux:`

### exp-finance-terminal-web

[exp-finance-terminal-web/pom.xml](../../exp-finance-terminal-web/pom.xml)

Coordinates: `io.fireflysoftware:exp-finance-terminal-web:26.10.6`. Parent: `io.fireflysoftware:exp-finance-terminal:26.10.6`.

- `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:`
- `io.fireflysoftware:exp-finance-terminal-core:`
- `org.fireflyframework:fireflyframework-security-resource-server:`
- `org.springdoc:springdoc-openapi-starter-webflux-api:${springdoc-openapi.version}`
- `org.springframework.boot:spring-boot-starter-actuator:`
- `org.springframework.boot:spring-boot-starter-test:`
- `org.springframework.boot:spring-boot-starter-webflux:`

### exp-finance-terminal

[pom.xml](../../pom.xml)

Coordinates: `io.fireflysoftware:exp-finance-terminal:26.10.6`. Parent: `io.fireflysoftware:firefly-parent:26.10.6`.

- `io.fireflysoftware:exp-finance-terminal-core:${project.version}`
- `io.fireflysoftware:exp-finance-terminal-sdk:${project.version}`
- `io.fireflysoftware:exp-finance-terminal-web:${project.version}`
- `io.fireflysoftware:firefly-bom:26.10.6`

## Test sources

- [exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/DomainConfigurationClientTest.java](../../exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/DomainConfigurationClientTest.java)
- [exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/DomainDistributorClientTest.java](../../exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/DomainDistributorClientTest.java)
- [exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/DomainInstructionClientTest.java](../../exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/DomainInstructionClientTest.java)
- [exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/DomainRecordClientTest.java](../../exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/DomainRecordClientTest.java)
- [exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/InstructionDeskTest.java](../../exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/InstructionDeskTest.java)
- [exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/SecurityCenterSandbox.java](../../exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/SecurityCenterSandbox.java)
- [exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/StaffTerminalTest.java](../../exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/StaffTerminalTest.java)
- [exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalFixture.java](../../exp-finance-terminal-core/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalFixture.java)
- [exp-finance-terminal-sdk/src/test/java/io/fireflysoftware/experience/finance/terminal/sdk/GeneratedTerminalClientContractTest.java](../../exp-finance-terminal-sdk/src/test/java/io/fireflysoftware/experience/finance/terminal/sdk/GeneratedTerminalClientContractTest.java)
- [exp-finance-terminal-sdk/src/test/resources/InstructionsApi-original-public-signatures.txt](../../exp-finance-terminal-sdk/src/test/resources/InstructionsApi-original-public-signatures.txt)
- [exp-finance-terminal-sdk/src/test/resources/StaffTerminalApi-original-public-signatures.txt](../../exp-finance-terminal-sdk/src/test/resources/StaffTerminalApi-original-public-signatures.txt)
- [exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/OpenApiGenApplication.java](../../exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/OpenApiGenApplication.java)
- [exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalBoundaryHttpTest.java](../../exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalBoundaryHttpTest.java)
- [exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalDefaultDenyHttpTest.java](../../exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalDefaultDenyHttpTest.java)
- [exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalIndependentHttpTest.java](../../exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalIndependentHttpTest.java)
- [exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalMissingTrustHttpTest.java](../../exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalMissingTrustHttpTest.java)
- [exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalOpenApiContractTest.java](../../exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalOpenApiContractTest.java)
- [exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalPackagedStartupIT.java](../../exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalPackagedStartupIT.java)
- [exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalSessionAuthorityHttpTest.java](../../exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalSessionAuthorityHttpTest.java)
- [exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalStrictPolicyRequiredHttpTest.java](../../exp-finance-terminal-web/src/test/java/io/fireflysoftware/experience/finance/terminal/TerminalStrictPolicyRequiredHttpTest.java)
- [exp-finance-terminal-web/src/test/resources/legacy-terminal-openapi.yml](../../exp-finance-terminal-web/src/test/resources/legacy-terminal-openapi.yml)

## CI definitions

- [.github/workflows/maven-central.yml](../../.github/workflows/maven-central.yml)
- [.github/workflows/release.yml](../../.github/workflows/release.yml)
