# Testing Rules

These rules MUST be followed every time test code is generated in this repository.

## Framework
- Follow the standard practices in **JUnit**.
- Use **JUnit asserts** for assertions.

## Test Naming Conventions
- Use `camelCase` delimited by `_`, in the form:
  `nameOfTheTest_whatIsThatYouAreTesting_outcome`

## Test Coverage
- Generate **unit tests** for service and controller layers.
- Generate **integration tests**, including database (DB) integration.
