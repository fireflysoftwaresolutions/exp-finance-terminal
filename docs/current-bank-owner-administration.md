# Current Bank owner administration

The terminal exposes a staff read of one exact Bank enrollment owner. Organization remains the system of record. The terminal coordinates the organization domain, which calls the organization core through its original generated SDK. The response is an administrative observation, not authorization to post money, open a book, enable a currency, admit a product, or act for an applicant.

## Operation and scope

`GET /api/v1/staff/tenants/{tenantId}/banks/{bankId}/enrollments/{enrollmentId}/versions/{version}/current`

All four selectors are required, and the enrollment version must be positive. Use identifiers and the version returned by the governed organization lifecycle. The endpoint neither creates an enrollment nor guesses the current version from an unrelated record. It returns the exact owner identity, allowed administrative uses, current control and generation values, the original native deadline, and a separate effective observation deadline. Responses use `Cache-Control: no-store`.

The existing terminal authentication chain verifies the staff token and projects its signed tenant and staff identity. The new controller does not accept an actor header or promote request data into a principal. Current policy is required both before and after the domain observation. Existing CSRF, session and other staff routes retain their original behavior.

The internal domain call uses a separate registered client-credentials identity. Staff tokens are not forwarded as service credentials. The domain independently checks its authenticated caller and current policy, obtains its own narrow core credential, and calls the genuine organization SDK. A configured client or a successful metadata build does not establish that the downstream owner is current.

## Configuration

The feature is off by default. Activate `current-bank-owner` only after providing actual caller trust, machine registration and current policy. This profile excludes unused JDBC, R2DBC, data-R2DBC and Flyway autoconfiguration for this stateless read; it does not supply a dummy database or activate another desk's persistence.

| Setting | Required meaning |
| --- | --- |
| `firefly.terminal.bank-owner.enabled` | Explicit activation of this read path. |
| `firefly.terminal.security.issuer`, `audience`, `jwk-set-uri` | The real signed staff trust configuration. |
| `firefly.security.resource-server.strict-policy` | Must be true. |
| `firefly.security.resource-server.tenant-required` | Must be true with the signed tenant projection. |
| `firefly.terminal.bank-owner.machine.tenant-id`, `bank-id` | Exact configured resource scope. |
| `firefly.terminal.bank-owner.machine.registration-id` | `organization-domain-owner-read`, matching Spring's OAuth registration. |
| `firefly.terminal.bank-owner.machine.base-uri` | Organization-domain HTTPS root. |
| Machine `token-uri`, `jwks-uri`, `issuer`, `audience`, `subject`, `client-id`, `machine-tenant-id` | Independently configured token issuer, intended identity and tenant; the returned signed token is checked against these values. |
| `firefly.terminal.bank-owner.policy.base-uri`, `decision-path` | Actual current policy backend, with no default allow. |

The checked-in profile lists the corresponding `FIREFLY_TERMINAL_OWNER_*` and `FIREFLY_TERMINAL_STAFF_*` environment variables. Supply the client secret through the deployment environment; never place it in a request, source file, log or response. The Spring OAuth registration uses client credentials and its provider token URI must match the explicit machine profile. Public issuer, JWKS and service endpoints require HTTPS; the policy adapter permits HTTP only on loopback for an explicitly owned local qualification environment.

## Time and refusal behavior

One original operation budget covers initial policy, credential acquisition, domain HTTP, final policy and delivery. Each layer only narrows its absolute and monotonic limits. `X-Firefly-Operation-Deadline` on the generated internal domain call carries that limit, never identity or authority. The response preserves native `currentUntilEpochMillis` separately from the earlier `observationUntilEpochMillis`; receiving it does not renew either deadline.

| HTTP status | Meaning |
| --- | --- |
| `400` | Invalid required selector or nonpositive exact version. |
| `401` | The original terminal authentication chain refuses missing or invalid signed credentials. |
| `403` | Signed staff scope or current local policy is insufficient. |
| `409` | The exact downstream owner is no longer current, including suspension. |
| `503` | A required dependency, trusted observation or finite delivery window is unavailable. |

Do not cache a successful response for later use or turn a conflict into a fabricated active owner. A new request must pass current authentication and policy again. No write, issuer dispatch or financial retry is performed by this read.

## Delivery boundary

Production services use interface/implementation separation and genuine generated clients. The full terminal application is compiled from its current source for the bounded HTTPS journey, with explicit unchanged packaged dependencies for the preexisting desks. This does not requalify those desks or their live providers. The original terminal OpenAPI producer and updated terminal SDK generation remain a distinct delivery step; an ordinary external HTTPS call does not count as terminal-SDK acceptance. Real environment activation and runtime outcomes must be recorded separately from source tests.
