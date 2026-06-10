import http from 'k6/http';
import { check, group } from 'k6';
import { Trend } from 'k6/metrics';

// Validates APAC-2847 / AC7:
//   "The endpoint must respond within 500ms at p95 under normal load.
//    (Load definition: 50 concurrent users, defined by performance team)"

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const POLICIES_PATH = '/api/v1/policies';

const VUS = Number(__ENV.VUS || 50);
const DURATION = __ENV.DURATION || '1m';
const P95_BUDGET_MS = Number(__ENV.P95_BUDGET_MS || 500);

// Seed ids from src/main/resources/data.sql — used to exercise the by-id path.
const POLICY_IDS = [
  '11111111-1111-1111-1111-111111111111',
  '22222222-2222-2222-2222-222222222222',
  '44444444-4444-4444-4444-444444444444',
  '88888888-8888-8888-8888-888888888888',
  '99999999-9999-9999-9999-999999999999',
];

const listDuration = new Trend('list_policies_duration', true);
const byIdDuration = new Trend('policy_by_id_duration', true);
const summaryDuration = new Trend('policy_summary_duration', true);

export const options = {
  scenarios: {
    normal_load: {
      executor: 'constant-vus',
      vus: VUS,
      duration: DURATION,
    },
  },
  thresholds: {
    // AC7: p95 under 500ms across the policy endpoints.
    http_req_duration: [`p(95)<${P95_BUDGET_MS}`],
    list_policies_duration: [`p(95)<${P95_BUDGET_MS}`],
    http_req_failed: ['rate<0.01'],
  },
};

function listPolicies() {
  const page = Math.floor(Math.random() * 3);
  const res = http.get(`${BASE_URL}${POLICIES_PATH}?page=${page}&size=10`, {
    tags: { endpoint: 'list' },
  });
  listDuration.add(res.timings.duration);
  check(res, {
    'list: status is 200': (r) => r.status === 200,
    'list: has content array': (r) => {
      try {
        return Array.isArray(r.json('content'));
      } catch (_) {
        return false;
      }
    },
  });
}

function policyById() {
  const id = POLICY_IDS[Math.floor(Math.random() * POLICY_IDS.length)];
  const res = http.get(`${BASE_URL}${POLICIES_PATH}/${id}`, {
    tags: { endpoint: 'by-id' },
  });
  byIdDuration.add(res.timings.duration);
  check(res, {
    'by-id: status is 200': (r) => r.status === 200,
  });
}

function policySummary() {
  const res = http.get(`${BASE_URL}${POLICIES_PATH}/summary`, {
    tags: { endpoint: 'summary' },
  });
  summaryDuration.add(res.timings.duration);
  check(res, {
    'summary: status is 200': (r) => r.status === 200,
  });
}

export default function () {
  // The list endpoint is the AC7 target, so weight it most heavily.
  group('GET /api/v1/policies', listPolicies);
  group('GET /api/v1/policies/{id}', policyById);
  group('GET /api/v1/policies/summary', policySummary);
}
