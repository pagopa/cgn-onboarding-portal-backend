# Project Guidelines

Keep this file and the related files under `.github/instructions/` updated whenever the project structure, stack, workflows, or conventions change.

## Stack

- Maven-based Spring Boot 2.7.18 backend targeting Java 21.
- Handwritten application code lives under `src/main/java/it/gov/pagopa/cgn/portal` and resources under `src/main/resources`.
- Persistence uses Spring Data JPA, PostgreSQL, and Flyway; Hibernate schema validation is enabled via `spring.jpa.hibernate.ddl-auto=validate`.
- The runtime also includes Spring Security, Quartz jobs, Thymeleaf templates/resources, Azure Blob/APIM integrations, mail, reCAPTCHA, and EYCA export integrations.

## Architecture

- Keep the existing request flow: generated OpenAPI interface -> controller -> facade -> service -> repository/converter/entity.
- Controllers mainly implement generated `it.gov.pagopa.cgnonboardingportal.*.api` interfaces and should stay thin.
- Put orchestration in facades and domain/business rules in services; keep mapping logic in the existing converter layer.
- Keep new code in the established packages such as `controller`, `facade`, `service`, `repository`, `converter`, `model`, `scheduler`, `security`, and `config`.

## Source Of Truth

- OpenAPI specifications under `openapi/` are the source of truth for API contracts.
- The packages under `it.gov.pagopa.cgnonboardingportal.*` are generated during Maven builds into `target/generated-sources`; do not treat `target/` as handwritten source.
- When contract changes are required, update the relevant OpenAPI spec first, then adapt handwritten controllers, facades, services, and converters.

## Agreement State Model

- `agreement.state` (`AgreementStateEnum`) is the canonical persisted source of truth for the agreement/convention lifecycle; do not introduce a parallel persisted status axis for the same domain.
- The canonical lifecycle includes onboarding states (`DRAFT`, `PENDING`, `APPROVED`, `REJECTED`) and post-approval operational states (`ACTIVE`, `INACTIVE`, `TERMINATION_IN_PROGRESS`, `TERMINATED`).
- Backoffice `AssignedAgreement` remains a DTO-only projection of `PENDING` plus `backofficeAssignee`; it is not a persisted agreement state.
- `/agreement-requests` stays a `PENDING`-only queue/slice. `/approved-agreements` is the broader post-approval backoffice surface and can include `APPROVED`, `ACTIVE`, `INACTIVE`, `TERMINATION_IN_PROGRESS`, and `TERMINATED`.
- `OrganizationStatus` is derived at read time and should follow the canonical agreement lifecycle semantics rather than legacy `APPROVED -> ACTIVE` shorthand or removed `Enabled` semantics.
- Publishing a valid current discount moves the agreement to `ACTIVE`; if the agreement is in `TERMINATION_IN_PROGRESS`, the same publish flow reactivates it back to `ACTIVE`.
- Operator access to agreement-scoped endpoints under `/agreements/{agreementId}/...` must be blocked when the agreement is `TERMINATED`; the bootstrap operator read on `POST /agreements` remains readable so the frontend can still receive the current agreement state.
- The backoffice termination API is command-based: `POST /approved-agreements/{agreementId}/termination` with `AgreementTerminationCommand` actions `StartTerminationInProgress`, `CancelTerminationInProgress`, and `CompleteTermination`.

## Build And Test

- Use Maven from the repository root.
- Prefer `mvn test` for the test suite and `mvn clean install` or `mvn clean verify` when a full build is needed.
- Most tests are Spring Boot integration tests built around `IntegrationAbstractTest`, Testcontainers-backed infrastructure, and MockMvc.
- When changing entities, repositories, or SQL, keep Flyway migrations aligned so the application still passes Hibernate validation.

## Working Rules

- Reuse existing enums, error models, and converters before introducing parallel DTO or state definitions.
- Do not hand-edit build output under `target/`.
- Check for existing tests in the matching package before adding new patterns or utilities.