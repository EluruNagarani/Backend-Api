# k6 Load Tests

Load tests that validate **AC7** of APAC-2847: the policy endpoints must respond
within **500ms at p95** under **50 concurrent users**.

## Prerequisites

- [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/) installed
  (`winget install k6 --source winget` on Windows, or `brew install k6` on macOS).
- The app running and reachable (default `http://localhost:8080`):

  ```sh
  ./mvnw spring-boot:run
  ```

## Running

```sh
# Default: 50 VUs for 1 minute, p95 budget 500ms
k6 run k6/load-test.js
```

Override defaults with environment variables:

```sh
# Point at a different host
k6 run -e BASE_URL=http://localhost:9090 k6/load-test.js

# Change the load definition or budget
k6 run -e VUS=100 -e DURATION=2m -e P95_BUDGET_MS=500 k6/load-test.js
```

## What it checks

The run **fails** (non-zero exit) if any threshold is breached:

| Threshold                | Meaning                                  |
| ------------------------ | ---------------------------------------- |
| `http_req_duration p95`  | Overall p95 latency < 500ms (AC7).       |
| `list_policies_duration` | p95 of `GET /api/v1/policies` < 500ms.   |
| `http_req_failed`        | Error rate below 1%.                     |

This makes the test suitable for a CI performance gate.
