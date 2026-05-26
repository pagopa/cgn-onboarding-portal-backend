---
description: "Use when editing OpenAPI contracts, generated API models/interfaces, controller signatures, or DTO mappings for the operator, backoffice, public, or EYCA APIs."
name: "OpenAPI Contract Guidelines"
applyTo:
  - "openapi/*.yaml"
  - "openapi/**/*.yaml"
---

# OpenAPI Contract Guidelines

- Treat the files under `openapi/` as the source of truth for API contracts.
- `openapi/openapi.yaml` drives the main operator API, `openapi/backoffice/openapi.yaml` drives backoffice APIs, `openapi/public.yaml` drives public APIs, and `openapi/eyca-data-export/openapi.yaml` drives the EYCA client generation.
- Maven generates the related `it.gov.pagopa.cgnonboardingportal.*` sources into `target/generated-sources`; do not hand-edit generated output.
- When a spec changes, check the handwritten controller that implements the generated interface and the converter/facade classes that map between JPA entities and generated models.
- Keep contract changes within the correct API surface instead of mixing operator, backoffice, public, and EYCA models.