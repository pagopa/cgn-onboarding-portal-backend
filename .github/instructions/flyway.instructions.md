---
description: "Use when adding or modifying Flyway SQL migrations, repeatable view scripts, or schema-related SQL under src/main/resources/db/migration."
name: "Flyway Migration Guidelines"
applyTo: "src/main/resources/db/migration/*.sql"
---

# Flyway Migration Guidelines

- Database changes live under `src/main/resources/db/migration`.
- Follow the existing naming scheme: `VNNN__description.sql` for versioned migrations and `R__description.sql` for repeatable objects such as views or materialized views.
- Preserve migration history: add a new `V...` file for new schema or data changes instead of rewriting an old applied versioned migration.
- Keep JPA entity/repository changes aligned with migrations because the application runs with `spring.jpa.hibernate.ddl-auto=validate`.