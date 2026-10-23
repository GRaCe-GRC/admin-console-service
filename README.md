# admin-console-service

Shared backend for the GRaCe-GRC admin console: review workflows and configuration for all projects.

> **Status:** backs the authorities dashboard of the admin console, through the
> [regulatory ingestion service](https://github.com/GRaCe-GRC/regulatory-ingestion-service).

## Stack
- Java (latest LTS at setup), Spring Boot (latest stable at setup)
- Maven, via the Maven wrapper (`./mvnw`)

## Getting started
The service calls the regulatory ingestion service (which calls the Regulatory API). To run them
on one machine, start those first (see their READMEs), then this service on another port:

```bash
SERVER_PORT=8082 ./mvnw spring-boot:run
```

| Environment variable | Default | Description |
|---|---|---|
| `REGULATORY_INGESTION_SERVICE_URL` | `http://localhost:8081` | Base URL of the regulatory ingestion service |

| Endpoint | Description |
|---|---|
| `GET http://localhost:8080/actuator/health` | Health status (`UP`) |
| `GET http://localhost:8080/actuator/info` | Application info |
| `GET http://localhost:8080/swagger-ui.html` | API documentation (OpenAPI at `/v3/api-docs`) |

## Dashboard API

Endpoints are shaped around the admin console screens, not the domain.

| Method and path | Body | Response |
|---|---|---|
| `GET /api/dashboard/authorities` | | `200` with the list of authorities for the dashboard table |
| `GET /api/dashboard/authorities/{id}` | | `200` with the authority |
| `POST /api/dashboard/authorities` | `name`, `jurisdiction` | `201`, empty body, `Location` header |
| `DELETE /api/dashboard/authorities/{id}` | | `204`, empty body |

An authority is `{id, name, jurisdiction, createdAt, enabled, createdBy}`. New authorities get
`createdBy = admin-console` until authentication exists. Requests are validated here (`400` with
an `errors` field per property); `4xx` problem details from the ingestion service (`404`, `409`
duplicate name) are returned unchanged; when it fails, cannot be reached or returns no `Location`,
the response is `502`. Calls to the ingestion service are in
`boot/client/RegulatoryIngestionServiceClient`.

## Test and build
```bash
./mvnw verify
```

Branch verify runs the same command on every push to a branch and on every pull request, through the
shared [Maven CI workflow](https://github.com/GRaCe-GRC/.github/blob/main/.github/workflows/maven-ci.yml) (`.github/workflows/branch-verify.yml`).

## Versions and images
Versions follow semantic versioning and come from the commit messages. Every merge to `main`
runs `.github/workflows/release.yml`: the CI again, then the shared
[release workflow](https://github.com/GRaCe-GRC/.github/blob/main/.github/workflows/README.md#release):

| Commit | Release |
|---|---|
| `fix: GRC-<n>: ...` | Patch |
| `feat: GRC-<n>: ...` | Minor |
| `chore:`, `docs:`, ... | No release |

A release creates the `vX.Y.Z` tag, a GitHub Release with notes, and the image
`ghcr.io/grace-grc/admin-console-service:<version>` (also tagged `latest`). The version is not stored in
`pom.xml`: it uses `${revision}`, which is `0.0.1-SNAPSHOT` for local builds and the release
version in CI.

```bash
docker pull ghcr.io/grace-grc/admin-console-service:latest
```

## Architecture
This service serves all three projects. Their architecture diagrams show one admin console
service per project; those are delivered as this single shared service. See the
[GRaCe-GRC documentation hub](https://github.com/GRaCe-GRC/GRaCe-GRC-docs).

## Contributing
See the organization's [contributing guide](https://github.com/GRaCe-GRC/.github/blob/main/CONTRIBUTING.md).
Commit messages use Conventional Commits with the ticket ID, for example
`feat: GRC-13: add review workflow endpoint`.

## License
Licensed under the [Apache License 2.0](LICENSE).
