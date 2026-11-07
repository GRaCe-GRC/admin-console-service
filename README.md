# admin-console-service

Shared backend for the GRaCe-GRC admin console: review workflows and configuration for all projects.

> **Status:** backs the dashboard of the admin console, through the
> [regulatory ingestion service](https://github.com/GRaCe-GRC/regulatory-ingestion-service), the
> [Control API](https://github.com/GRaCe-GRC/control-api) and the
> [Control Mapping API](https://github.com/GRaCe-GRC/control-mapping-api).

## Stack
- Java (latest LTS at setup), Spring Boot (latest stable at setup)
- Maven, via the Maven wrapper (`./mvnw`)

## Getting started
The service calls the regulatory ingestion service (which calls the Regulatory API), the Control
API and the Control Mapping API. To run them on one machine, start those first (see their READMEs), then this
service on another port:

```bash
SERVER_PORT=8082 ./mvnw spring-boot:run
```

| Environment variable | Default | Description |
|---|---|---|
| `REGULATORY_INGESTION_SERVICE_URL` | `http://localhost:8081` | Base URL of the regulatory ingestion service |
| `CONTROL_API_URL` | `http://localhost:8083` | Base URL of the Control API |
| `CONTROL_MAPPING_API_URL` | `http://localhost:8084` | Base URL of the Control Mapping API |

| Endpoint | Description |
|---|---|
| `GET http://localhost:8080/actuator/health` | Health status (`UP`) |
| `GET http://localhost:8080/actuator/info` | Application info |
| `GET http://localhost:8080/swagger-ui.html` | API documentation (OpenAPI at `/v3/api-docs`) |

## Dashboard API

Endpoints are shaped around the admin console screens, not the domain.

| Method and path | Body | Response |
|---|---|---|
| `GET /api/dashboard` | | `200` with the whole dashboard (see below) |
| `POST /api/dashboard/authorities` | `name`, `jurisdiction` | `201`, empty body, `Location` header |
| `DELETE /api/dashboard/authorities/{id}` | | `204`, empty body |
| `POST /api/dashboard/frameworks` | `authorityId`, `name`, `shortName`, `citation` | `201`, empty body, `Location` header (`/api/dashboard/frameworks/{id}`) |
| `DELETE /api/dashboard/frameworks/{id}` | | `204`, empty body |

New authorities and frameworks get `createdBy = admin-console` until authentication exists.

`GET /api/dashboard` reads seven unfiltered lists in parallel (on virtual threads): authorities,
frameworks, files and requirements from the ingestion service, control groups and controls from the
Control API, and control mappings from the Control Mapping API, in
`domain/service/DashboardService`. `domain/mapper/DashboardMapper` puts them together:

```text
{ authorities: [ { id, name, jurisdiction, enabled, createdAt, createdBy,
    frameworks: [ { id, name, shortName, citation, enabled, createdAt, createdBy, coverage,
      files: [ { id, title, sourceUrl, versionLabel, publicationDate, createdAt, createdBy, coverage,
        requirements: [ { id, provisionRef, heading, text, createdAt, createdBy,
          mappedControls: [ { mappingId, controlId, controlRef, title, rationale } ],
          children: [ requirement ] } ] } ] } ] } ],
  controlGroups: [ { id, groupRef, title, createdAt, createdBy,
    controls: [ { id, controlRef, title, statement, createdAt, createdBy,
      mappedRequirements: [ { mappingId, requirementId, provisionRef, heading, fileId, fileTitle,
        frameworkId, frameworkName, authorityId, authorityName, rationale } ],
      enhancements: [ control ] } ],
    children: [ group ] } ],
  ungroupedControls: [ control ] }
```

- Requirements are nested under their parent, control groups under their parent group, and
  enhancements under the control they enhance (they do not appear again at the top level).
  `ungroupedControls` holds the top-level controls without a group.
- `coverage` is `{mapped, total}`: `total` counts the lowest-level provisions of the file
  (requirements that are no other requirement's parent) and `mapped` how many of them have at least
  one mapping. A framework's coverage adds up its files; an empty file or framework has `{0, 0}`.
- Mappings whose requirement or control no longer exists are left out.
- Lists are sorted: authorities and frameworks by name, files by publication date then title,
  requirements by `provisionRef`, groups by `groupRef`, controls by `controlRef`. Empty lists are
  `[]`.

Requests are validated here (`400` with an `errors` field per property); `4xx` problem details from
the called services (for example `404` unknown authority or framework, `409` duplicate name or a
framework that still has files) are returned unchanged; when one of them fails, cannot be reached
or the ingestion service returns no `Location`, the response is `502`. The HTTP calls are in
`boot/client` (`RegulatoryIngestionServiceClient`, `ControlApiClient`, `ControlMappingApiClient`).

```bash
curl -s http://localhost:8082/api/dashboard
curl -s -i -H 'Content-Type: application/json' \
  -d '{"authorityId":"<authority id>","name":"Digital Operational Resilience Act","shortName":"DORA","citation":"Regulation (EU) 2022/2554"}' \
  http://localhost:8082/api/dashboard/frameworks
curl -s -i -X DELETE http://localhost:8082/api/dashboard/frameworks/<framework id>
```

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
