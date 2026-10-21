<a href="https://fireflysoftware.io/openplatform/">
<picture>
  <source media="(max-width: 640px) and (prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/fireflysoftwaresolutions/.github/main/profile/assets/banner-narrow-dark.svg">
  <source media="(max-width: 640px)" srcset="https://raw.githubusercontent.com/fireflysoftwaresolutions/.github/main/profile/assets/banner-narrow-light.svg">
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/fireflysoftwaresolutions/.github/main/profile/assets/banner-dark.svg">
  <img src="https://raw.githubusercontent.com/fireflysoftwaresolutions/.github/main/profile/assets/banner-light.svg" alt="Firefly OpenPlatform - Experience - Finance Terminal" width="100%">
</picture>
</a>

# Firefly OpenPlatform - Experience - Finance Terminal

Developed by the Firefly Software Foundation. [dev@fireflysoftware.io](mailto:dev@fireflysoftware.io)

[Website](https://fireflysoftware.io/) · [OpenPlatform](https://fireflysoftware.io/openplatform/) · [Module catalogue](https://fireflysoftware.io/openplatform/modules/)

Staff terminal for financial administration. An employee signs in. A customer subject cannot open this terminal.

Repository: [https://github.com/fireflysoftwaresolutions/exp-finance-terminal](https://github.com/fireflysoftwaresolutions/exp-finance-terminal)

Version 26.10.3.

## Employee and customer

Customer journeys keep using the customer subject from domain-core-security-center. This terminal does not reuse that subject as the operator.

Sign-in calls `POST /api/v1/identity/subjects`, then `POST /api/v1/access/checks` with action `ADMINISTER`. The security center decides. This service does not keep a second grant table.

`firefly.security-center.identity.adapter` stays on the security center and defaults to `sandbox`. The sandbox usernames are:

| Username | Actor | What the shipped policy allows |
| --- | --- | --- |
| `sample-employee` | `33333333-3333-4333-8333-333333333333` | `READ` on the sample resources, plus `ADMINISTER` and `IMPERSONATE` |
| `sample-journey` | `11111111-1111-4111-8111-111111111111` | `READ` and `CREATE` only. Not `ADMINISTER`. Not `IMPERSONATE`. |
| `denied-actor` | `22222222-2222-4222-8222-222222222222` | No grants |

The file an installer replaces for those grants is `domain-core-security-center-core/src/main/resources/access/sandbox-access-policy.json`. Point `firefly.security-center.access.sandbox-policy` at another file of the same shape when the deployment leaves the sample. Leave `firefly.security-center.identity.adapter` at `sandbox` until a Jafly provider (`keycloak`, `cognito`, or `internal-db`) is selected on the security center.

## Impersonation

A command that acts for a customer calls the access check with action `IMPERSONATE`, resource type `PARTY`, and the customer party id before the domain port runs. A denial does not call the domain. The audit entry stores the employee id and the customer party id, plus the reason and the action. The domain command receives the customer party id as the business party and the employee id as the actor.

The sample customer party in the sandbox book is `10000000-0000-4000-8000-000000000001`.

## Journeys

The terminal lists and opens records, updates them through a domain port, and closes them with the status configured below. It does not call a core SDK.

| Area | Domain | Close status |
| --- | --- | --- |
| Accounts | domain-banking-accounts | `CLOSED` |
| Cards | domain-banking-cards. Revolving lines stay there. This terminal does not add a domain for core-lending-credit-cards. | `CANCELLED` |
| Lending applications | domain-lending-loan-origination | `WITHDRAWN` |
| Mortgage agreements | domain-lending-mortgage | `CLOSED` |
| Supply chain finance agreements | domain-lending-supply-chain-finance. Removal runs only when the agreement is `DRAFT`. | `CLOSED` |
| Collateral cases | domain-lending-collateral-management | `RELEASED` |
| Collection cases | domain-lending-delinquency-collections | `CLOSED` |
| Provisioning cases | domain-lending-provisioning-risk | `RELEASED` |
| AML cases | domain-lending-regulatory-compliance | `CLOSED` |

Account update posts the customer as an account holder. Card update calls the card security operation. A lending application update calls employment data on the origination domain. Saga records are read and written with the domain's get and update operations. Close calls the domain close, cancel, or withdraw operation that matches the area.

## Configuration and master data

Sandbox settings are not stored in a terminal database. `SandboxConfigurationStore` is the in-memory stand-in for environment configuration in core-common-config-mgmt. Reference currencies and countries are read through the same port. Set `firefly.terminal.adapter=domain` to call core-common-config-mgmt and core-common-reference-master-data instead.

An installer sets these keys:

| Key | Sample value |
| --- | --- |
| `finance-terminal.close-status.ACCOUNT` | `CLOSED` |
| `finance-terminal.close-status.CARD` | `CANCELLED` |
| `finance-terminal.close-status.LENDING_APPLICATION` | `WITHDRAWN` |
| `finance-terminal.close-status.MORTGAGE_AGREEMENT` | `CLOSED` |
| `finance-terminal.close-status.SUPPLY_CHAIN_FINANCE_AGREEMENT` | `CLOSED` |
| `finance-terminal.close-status.COLLATERAL_CASE` | `RELEASED` |
| `finance-terminal.close-status.COLLECTION_CASE` | `CLOSED` |
| `finance-terminal.close-status.PROVISIONING_CASE` | `RELEASED` |
| `finance-terminal.close-status.AML_CASE` | `CLOSED` |
| `finance-terminal.remove-status.SUPPLY_CHAIN_FINANCE_AGREEMENT` | `DRAFT` |
| `finance-terminal.accounts.holder-role-id` | role UUID used when adding the customer as an account holder |
| `finance-terminal.cards.security-setting-id` | card security setting UUID |
| `finance-terminal.distributor.channel` | `BRANCH` |
| `finance-terminal.records.ACCOUNT` and the same shape for each area | comma-separated record ids |
| `finance-terminal.customer-party-id` | `10000000-0000-4000-8000-000000000001` |

## Distributors

Brokers, agencies, and collaborators are reached through core-common-distributor-mgmt. The staff terminal lists them and attaches one distributor UUID to a lending application or another record. It does not create a second agency master and it does not drop the existing distributor tables.

The distributor row is still a goods and lending catalog entry. It has no party-role column. `domain-distributor-catalog` remains a product and shipment catalog. The sandbox directory labels the sample parties `BROKER`, `AGENCY`, and `COLLABORATOR`. The domain adapter uses `externalCode` when that value is one of those three words, and otherwise labels the row `DISTRIBUTOR`. Agency id and collaborator id come from the existing agency and agent resources. Channel comes from `finance-terminal.distributor.channel`. The customer party id on an attachment is the party being impersonated.

## Adapter an installer replaces

| Default class | Replacement |
| --- | --- |
| `SandboxRecordBook` | `DomainRecordClient` |
| `SandboxConfigurationStore` | `DomainConfigurationClient` |
| `SandboxDistributorDirectory` | `DomainDistributorClient` |

Select the replacements with `firefly.terminal.adapter=domain`. Identity and access stay on the HTTP calls to domain-core-security-center in either mode. `InMemoryAuditLog` keeps the impersonation audit of both ids. Replace that class when a deployment must ship the same entries to another system. The class is complete for the sample.

## API

Base path `/api/v1/staff`. Send `X-Actor-Id` after sign-in.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/sessions` | Sign in with `{ "username": "sample-employee" }` |
| `GET` | `/records/{area}` | List records for an area |
| `GET` | `/records/{area}/{recordId}` | Open one record |
| `PUT` | `/records/{area}/{recordId}` | Update through the domain port after impersonation |
| `POST` | `/records/{area}/{recordId}/close` | Close using the configured status |
| `POST` | `/records/{area}/{recordId}/remove` | Remove a supply chain finance draft |
| `POST` | `/records/{area}/{recordId}/decide` | Approve or reject a lending application |
| `POST` | `/records/{area}/{recordId}/distributor` | Attach a distributor UUID |
| `GET` | `/distributors` | List distributors |
| `PUT` | `/configuration/{key}` | Set an environment property |
| `GET` | `/masters/{type}` | List `CURRENCY` or `COUNTRY` |

Areas: `ACCOUNT`, `CARD`, `LENDING_APPLICATION`, `MORTGAGE_AGREEMENT`, `SUPPLY_CHAIN_FINANCE_AGREEMENT`, `COLLATERAL_CASE`, `COLLECTION_CASE`, `PROVISIONING_CASE`, `AML_CASE`.

## Running locally

Java 25 and Maven 3.9+. domain-core-security-center on port 8085 with its default sandbox identity adapter.

```bash
mvn spring-boot:run -pl exp-finance-terminal-web
```

The terminal listens on port 8120.
