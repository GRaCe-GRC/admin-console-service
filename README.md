# admin-console-service

Shared backend for the GRaCe-GRC admin console: review workflows and configuration for all projects.

> **Status:** starter app. It starts a Spring Boot application with Actuator endpoints and nothing else. Features arrive in later stories.

## Stack
- Java (latest LTS at setup), Spring Boot (latest stable at setup)
- Maven, via the Maven wrapper (`./mvnw`)

## Getting started
```bash
./mvnw spring-boot:run
```

| Endpoint | Description |
|---|---|
| `GET http://localhost:8080/actuator/health` | Health status (`UP`) |
| `GET http://localhost:8080/actuator/info` | Application info |

## Test and build
```bash
./mvnw verify
```

CI runs the same command on every pull request through the shared
[Maven CI workflow](https://github.com/GRaCe-GRC/.github/blob/main/.github/workflows/maven-ci.yml)
(`.github/workflows/ci.yml`).

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
