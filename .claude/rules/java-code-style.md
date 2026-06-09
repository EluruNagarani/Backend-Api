# Java Code Style Rules

These rules MUST be followed every time Java code is generated in this repository.

## Naming Conventions
- Follow standard Java conventions.
- `camelCase` for methods and variables.
- `PascalCase` for class and interface names.
- `UPPER_SNAKE_CASE` for constants.

## Size Limits
- No file may exceed **200 lines**.
- No method may exceed **50 lines**.

## Design Decisions
- **No magic numbers or magic strings** — extract them into named constants.
- **No boolean parameters in methods** — they violate the Single Responsibility Principle (SRP). Split into separate methods or use a dedicated type/enum.
- **Cyclomatic complexity must be < 4** for `if-else` and `switch-case` blocks. Higher complexity violates the Open/Closed Principle (OCP); use the **Strategy pattern** instead.
- **No early return values when exceptions are thrown** — do not mix early `return` statements with exception-throwing flow.

## Null Handling
- Use `Optional` instead of `null` wherever applicable.

## Comments
- **Do not add comments (including Javadoc) above methods, classes, enums, records, or
  fields.** They clutter the code. Names must be self-explanatory instead.
- Only add a comment when the *why* behind a line is genuinely non-obvious — never to
  restate what the code already says.
