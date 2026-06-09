# APAC-2847
# Policy Overview Dashboard — Backend API (BFF Layer)
# ─────────────────────────────────────────────────────────────
# Story
# ─────────────────────────────────────────────────────────────

As an APAC operations user,
I want to see a list of my active insurance policies on the dashboard,
So that I can quickly review policy status and take action where needed.

# ─────────────────────────────────────────────────────────────
# Background
# ─────────────────────────────────────────────────────────────

The Policy Overview Dashboard is a new Angular frontend being built
by the UI team. They need a BFF endpoint that aggregates and transforms
policy data from the core policy database into a frontend-friendly format.

Downstream: PolicyRepository (PostgreSQL via Spring Data JPA).
The UI team has confirmed they need a paginated list response —
they are using Angular Material table with server-side pagination.

Regions in scope: Singapore, Hong Kong, Australia, India, Japan.

# ─────────────────────────────────────────────────────────────
# Acceptance Criteria
# ─────────────────────────────────────────────────────────────

AC1 — Basic retrieval
  Given a logged-in user
  When they call GET /api/policies
  Then the response contains a paginated list of policies
  And each policy shows: policy number, holder name, region,
      status, premium with currency, and policy dates

AC2 — Pagination
  Given a request with ?page=0&size=10
  Then the response contains at most 10 records
  And includes totalElements and totalPages in the response

AC3 — Status display
  Given a policy with status ACTIVE
  Then the frontend receives "Active" not "ACTIVE"
  Given a policy with status LAPSED
  Then the frontend receives "Lapsed" not "LAPSED"

AC4 — Region display
  Given a policy from region SG
  Then the frontend receives "Singapore" not "SG"
  (Same rule applies for HK, AU, IN, JP)

AC5 — Expiry indicator
  Given a policy whose end date is within 30 days from today
  Then isExpiringSoon is true in the response

AC6 — Error handling
  Given the policy database is unreachable
  Then the API returns 503 with a readable error message
  And does not expose internal stack traces

AC7 — Performance
  The endpoint must respond within 500ms at p95 under normal load.
  (Load definition: 50 concurrent users, defined by performance team)

# ─────────────────────────────────────────────────────────────
# Out of scope for this ticket
# ─────────────────────────────────────────────────────────────

- Filtering by status or region (APAC-2901)
- Export to CSV (APAC-2934)
- Policy detail view (APAC-2848)
- Authentication implementation (handled by platform team, APAC-1200)
