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

## Build And Test

- Use Maven from the repository root.
- Prefer `mvn test` for the test suite and `mvn clean install` or `mvn clean verify` when a full build is needed.
- Most tests are Spring Boot integration tests built around `IntegrationAbstractTest`, Testcontainers-backed infrastructure, and MockMvc.
- When changing entities, repositories, or SQL, keep Flyway migrations aligned so the application still passes Hibernate validation.

## Working Rules

- Reuse existing enums, error models, and converters before introducing parallel DTO or state definitions.
- Do not hand-edit build output under `target/`.
- Check for existing tests in the matching package before adding new patterns or utilities.