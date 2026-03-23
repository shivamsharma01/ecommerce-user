# User service

User profile and account APIs backed by PostgreSQL; subscribes to **user-signup-events** over Pub/Sub when enabled.

## Requirements

- Java 17
- PostgreSQL for a real run

**Database schema** is applied by **Flyway Jobs** in `ecomm-infra/deploy/helm/mcart-bootstrap` (SQL under `files/user/`). The app does not run migrations on startup. For local PostgreSQL, see **`ecomm-infra/README.md`** §2 — local migrations.

## Run locally

```bash
./gradlew bootRun
```

Configure datasource and auth issuer (same OIDC issuer as other services):

| Variable | Purpose |
|----------|---------|
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | PostgreSQL |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | Same value as auth JWT `iss` |
| `SPRING_CLOUD_GCP_PROJECT_ID` | GCP project |
| `SPRING_CLOUD_GCP_PUBSUB_ENABLED` | `true` to use Pub/Sub |
| `USER_PUBSUB_ENABLED` | `true` to start the signup-event subscriber |
| `USER_PUBSUB_SUBSCRIPTION` | Subscription name (default `user-signup-events-sub`) |

## Build and test

```bash
./gradlew build
```

The `test` profile uses in-memory H2 (see `src/test/resources/application-test.yaml`) and disables Pub/Sub.

## Kubernetes

Manifests: **`ecomm-infra/deploy/k8s/apps/user/`**. Apply with `ecomm-infra/deploy/Makefile` (`make apps-apply`). Secrets: **`ecomm-infra/README.md`** §3.
