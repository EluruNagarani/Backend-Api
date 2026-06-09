# Logging Rules

These rules MUST be followed every time logging code is generated in this repository.

## Framework
- **Do not use `System.out.println`** (or `System.err.println`) for logging.
- Use **SLF4J** for all logging.

## Sensitive Information
- **Never log sensitive information**, including policy number and policy amount.

## Formatting
- **Do not concatenate strings** during logging. Use SLF4J parameterized messages (e.g. `log.info("value: {}", value)`).

## Required Context
- Every log statement should include: **method**, **path**, **request ID (correlation ID)**, and **duration**.

## Bulk Operations
- For bulk operations, log the **total count** and **page size**.
