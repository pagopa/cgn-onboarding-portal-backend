# Repository Instructions

## Scope And Maintenance

- This file is the single source of shared project instructions for coding agents working in this repository. Read it at the start of each session and follow the sections applicable to the task.
- When a task changes the project structure, stack, architecture, API contracts, agreement lifecycle, build/test workflows, or coding conventions described here, update the affected instructions in the same change before completing the task.
- Before completing implementation or review work, check whether the affected instructions still match the project. Correct stale guidance when necessary; do not add cosmetic updates for unchanged rules.
- Record lasting project conventions and verified behavior here, not temporary task progress or individual test results.
- Keep tool-specific instruction files as references to this file. Maintain shared rules here rather than copying them into separate files for each tool.

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
- The canonical lifecycle includes onboarding states (`DRAFT`, `PENDING`, `APPROVED`, `REJECTED`) and post-approval operational states (`ACTIVE`, `EXPIRED`, `INACTIVE`, `TERMINATION_REMINDER_SENT`, `TERMINATION_IN_PROGRESS`, `TERMINATED`).
- Backoffice `AssignedAgreement` remains a DTO-only projection of `PENDING` plus `backofficeAssignee`; it is not a persisted agreement state.
- `/agreement-requests` is the pre-approval/request backoffice surface and can include `DRAFT`, `PENDING`, and `REJECTED`; `AssignedAgreement` remains a DTO-only projection of `PENDING` plus `backofficeAssignee`. `/approved-agreements` is the broader post-approval backoffice surface and can include `APPROVED`, `ACTIVE`, `EXPIRED`, `INACTIVE`, `TERMINATION_REMINDER_SENT`, `TERMINATION_IN_PROGRESS`, and `TERMINATED`.
- `OrganizationStatus` is derived at read time and should follow the canonical agreement lifecycle semantics rather than legacy `APPROVED -> ACTIVE` shorthand or removed `Enabled` semantics.
- Publishing a valid current discount moves the agreement to `ACTIVE` in the allowed post-approval states; publish/republish must be rejected while the agreement is in `TERMINATION_IN_PROGRESS`.
- Operator access to agreement-scoped endpoints under `/agreements/{agreementId}/...` must be blocked when the agreement is `TERMINATED`; the bootstrap operator read on `POST /agreements` remains readable so the frontend can still receive the current agreement state.
- The backoffice termination API is command-based: `POST /approved-agreements/{agreementId}/termination` with `AgreementTerminationCommand` actions `SendTerminationReminder`, `StartTerminationInProgress`, `CancelTerminationInProgress`, and `CompleteTermination`. `SendTerminationReminder` accepts `APPROVED` or `INACTIVE` and moves the agreement to `TERMINATION_REMINDER_SENT`.

## Build And Test

- Use Maven from the repository root.
- Prefer `mvn test` for the test suite and `mvn clean install` or `mvn clean verify` when a full build is needed.
- Most tests are Spring Boot integration tests built around `IntegrationAbstractTest`, Testcontainers-backed infrastructure, and MockMvc.
- When changing entities, repositories, or SQL, keep Flyway migrations aligned so the application still passes Hibernate validation.

## Working Rules

- Write commit messages, including both the subject and body, in English even when the conversation is in Italian, unless the user explicitly requests another language.
- Reuse existing enums, error models, and converters before introducing parallel DTO or state definitions.
- Do not hand-edit build output under `target/`.
- Check for existing tests in the matching package before adding new patterns or utilities.
- Avoid manually pre-sizing collections with summed `size()` values before sequential `addAll(...)` calls; prefer default construction unless there is a clear measured need.
- In handwritten Java code, if a `List` or other collection is guaranteed non-null by the API contract or local control flow, prefer the native `isEmpty()` check instead of `CollectionUtils.isEmpty(...)`; use null-safe utility checks only when the collection may actually be null.
- When adding or changing handwritten Java code, including tests, keep database reads and writes out of stream operations and collection-processing lambdas or method references (such as `map`, `filter`, `peek`, and `forEach`). This also applies to helper or service calls that access the database indirectly.
- Prepare or transform data in memory, then perform database access explicitly outside the pipeline (for example, `saveAll` at the repository layer). Preserve the required transaction boundaries and error-handling behavior when moving or grouping calls. When per-element database calls are necessary, use an explicit loop. `saveAll` alone does not guarantee SQL batching or a single SQL statement.

## Java Test Guidelines

Applies when adding or updating tests under `src/test/java`, including Spring Boot integration tests, controller API tests, scheduler tests, and repository/service tests.

- Most repository, service, scheduler, and controller tests are `@SpringBootTest` integration tests that extend `IntegrationAbstractTest`.
- Reuse `IntegrationAbstractTest` for container-backed infrastructure and shared cleanup instead of creating a separate Testcontainers bootstrap.
- For controller/API tests, follow the existing MockMvc pattern with `@AutoConfigureMockMvc`; many existing endpoint tests use `addFilters = false` and auth helpers such as `setOperatorAuth()` or `setAdminAuth()` from the base test utilities.
- In Java tests that use Mockito, statically import core methods from `org.mockito.Mockito` and `org.mockito.ArgumentMatchers` instead of calling them as `Mockito.*` or `ArgumentMatchers.*`; prefer imports such as `mock`, `spy`, `when`, `verify`, `times`, `any`, and `anyString`.
- Reuse `TestUtils` and existing sample-entity helpers before creating new ad hoc fixture builders.
- Keep test package placement aligned with the main code package being exercised and keep class names in the existing `*Test` style.

## OpenAPI Contract Guidelines

Applies when editing OpenAPI contracts under `openapi/`, generated API models/interfaces, handwritten controller signatures, or DTO mappings for the operator, backoffice, public, and EYCA APIs.

- Treat the files under `openapi/` as the source of truth for API contracts.
- `openapi/openapi.yaml` drives the main operator API, `openapi/backoffice/openapi.yaml` drives backoffice APIs, `openapi/public.yaml` drives public APIs, and `openapi/eyca-data-export/openapi.yaml` drives the EYCA client generation.
- Maven generates the related `it.gov.pagopa.cgnonboardingportal.*` sources into `target/generated-sources`; do not hand-edit generated output.
- When a spec changes, check the handwritten controller that implements the generated interface and the converter/facade classes that map between JPA entities and generated models.
- Keep contract changes within the correct API surface instead of mixing operator, backoffice, public, and EYCA models.

## Flyway Migration Guidelines

Applies when adding or modifying Flyway SQL migrations, repeatable view scripts, or schema-related SQL under `src/main/resources/db/migration`.

- Database changes live under `src/main/resources/db/migration`.
- Follow the existing naming scheme: `VNNN__description.sql` for versioned migrations and `R__description.sql` for repeatable objects such as views or materialized views.
- Preserve migration history: add a new `V...` file for new schema or data changes instead of rewriting an old applied versioned migration.
- Keep JPA entity/repository changes aligned with migrations because the application runs with `spring.jpa.hibernate.ddl-auto=validate`.
