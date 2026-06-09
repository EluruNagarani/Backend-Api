# Tech Stack

The project MUST use the following technologies and versions. "Latest − 1" means one
minor/feature release behind the current latest, chosen for stability.

| Technology      | Version            | Notes                                         |
| --------------- | ------------------ | --------------------------------------------- |
| Java (JDK)      | 21 (LTS)           | Language level and runtime target.            |
| Spring Boot     | 3.4.x (latest − 1) | Current latest is 3.5.x; pin to 3.4.x.        |
| Build tool      | Maven 3.9.x        | Wrapper (`mvnw`) committed to the repo.       |
| Database        | PostgreSQL 16.x    | Primary datastore.                            |
| DB migrations   | Flyway 10.x        | Managed via `flyway-core` Spring Boot starter.|
| Testing         | JUnit 5 (Jupiter) 5.11.x | Bundled via `spring-boot-starter-test`. |

## Notes
- All versions are managed through the Spring Boot BOM where possible; only override
  when a starter does not pin the desired version.
- JUnit follows the practices defined in [.claude/rules/testing.md](../rules/testing.md).
