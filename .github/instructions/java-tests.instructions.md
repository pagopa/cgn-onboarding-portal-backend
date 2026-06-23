---
description: "Use when adding or updating tests under src/test/java, especially Spring Boot integration tests, controller API tests, scheduler tests, or repository/service tests."
name: "Java Test Guidelines"
applyTo: "src/test/java/**/*.java"
---

# Java Test Guidelines

- Most repository, service, scheduler, and controller tests are `@SpringBootTest` integration tests that extend `IntegrationAbstractTest`.
- Reuse `IntegrationAbstractTest` for container-backed infrastructure and shared cleanup instead of creating a separate Testcontainers bootstrap.
- For controller/API tests, follow the existing MockMvc pattern with `@AutoConfigureMockMvc`; many existing endpoint tests use `addFilters = false` and auth helpers such as `setOperatorAuth()` or `setAdminAuth()` from the base test utilities.
- Reuse `TestUtils` and existing sample-entity helpers before creating new ad hoc fixture builders.
- Keep test package placement aligned with the main code package being exercised and keep class names in the existing `*Test` style.