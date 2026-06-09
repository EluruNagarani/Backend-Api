# Architecture Rules

These rules MUST be followed every time code is generated in this repository. They
describe the layered architecture and the package structure that all code must live in.

## Layering Principles
- The architecture is **layered with a domain core**. Dependencies point **inward**:
  `api` → `service` → `domain`, and `infrastructure` → `domain`.
- The **`domain` layer has no dependencies** on `api`, `service`, `infrastructure`,
  frameworks, or persistence concerns. It holds the business models.
- The **`api` layer never talks to `infrastructure` directly** — it goes through
  `service`.
- **DTOs never cross out of the `api` layer**, and **persistence entities never cross
  out of the `infrastructure` layer**. Only `domain` models move between layers.
- Each boundary has an explicit **mapper** to translate between representations.

## Package Structure
All production code lives under the base package `com.chubb.assessment`, organized as:

```
[api]
    --- [controller]                REST controllers; thin, delegate to service.
    --- [dto]
            --- [request]           Inbound request DTOs.
            --- [response]          Outbound response DTOs.
    --- [mapper]
            --- RequestDtoToDomain  Maps request DTOs -> domain models.
            --- DomainToResponseDto Maps domain models -> response DTOs.
    --- [exception]                 @RestControllerAdvice handler(s) mapping
                                    exceptions -> HTTP responses/status codes.

[domain]
    --- [models]                    Pure business models. No framework/persistence deps.

[service]                           Business logic / use cases. Orchestrates domain.

[infrastructure]
    --- [persistence]
            --- [repository]        Spring Data repositories.
            --- [entity]            JPA entities (persistence representation).
            --- [mapper]
                    --- EntityToDomain  Maps entities <-> domain models.
    --- [cache]                     Caching adapters.

[config]                            Spring configuration classes.

[common]                            Cross-cutting concerns shared by all layers.
    --- [exception]                 Generic/technical exceptions used across layers
                                    (framework-free).
    --- [util]                      Stateless helpers (formatting, etc.).
    --- [logging]                   Logging and correlation ID concerns.
```

## Rules of Thumb
- **Controllers** stay thin: validate input, call a service, map the result. No business
  logic, no persistence access.
- **Services** contain business logic and operate on **domain models**, not DTOs or
  entities.
- **Mappers** are dedicated classes; do not inline mapping logic across layers.
- **Cross-cutting concerns** (logging, correlation ID handling — see
  [logging.md](logging.md)) belong in `common`.

## Exceptions and Utilities
- **Cross-layer utilities** live in `common/util`. They must be **stateless** and
  **dependency-free** — no imports from `api`, `service`, `infrastructure`, or `domain`.
  If a helper needs a domain model, it is not cross-cutting; push it into `domain`.
- **Generic/technical exceptions** shared across layers (e.g. `ResourceNotFoundException`,
  `ValidationException`) live in `common/exception` and stay **framework-free**.
- **Domain-specific exceptions** that express a business-rule violation (e.g.
  `PolicyNotEligibleException`) live in `domain`, next to the model they concern.
- **Exception handling** (translation to HTTP) is an `api` concern: the
  `@RestControllerAdvice` handler lives in `api/exception` and maps `common`/`domain`
  exceptions to response DTOs and HTTP status codes. The exception *types* never depend
  on the web layer.
- Honor the size, naming, null-handling, and design rules in
  [java-code-style.md](java-code-style.md) within every layer.
