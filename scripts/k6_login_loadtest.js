import http from "k6/http";
import { check, sleep } from "k6";
import { Trend } from "k6/metrics";

const baseUrl = __ENV.BASE_URL || "http://10.0.2.6:8080";
const emailPrefix = __ENV.EMAIL_PREFIX || "loadtest.user";
const emailDomain = __ENV.EMAIL_DOMAIN || "example.com";
const password = __ENV.PASSWORD || "Passw0rd!123";
const loadVus = Number(__ENV.LOAD_VUS || 10);
const loadDuration = __ENV.LOAD_DURATION || "1m";
const loadIterations = Number(__ENV.LOAD_ITERATIONS || 1);
const loadMode = String(__ENV.LOAD_MODE || "constant-vus").toLowerCase();
const useLoginQueue =
  String(__ENV.USE_LOGIN_QUEUE || "").toLowerCase() === "true" ||
  String(__ENV.USE_LOGIN_QUEUE || "") === "1";
const queuePollIntervalMs = Number(__ENV.LOGIN_QUEUE_POLL_MS || 1000);
const queueMaxWaitMs = Number(__ENV.LOGIN_QUEUE_MAX_WAIT_MS || 60000);

const queueWaitMsTrend = new Trend("queue_wait_ms");
const loginFlowMsTrend = new Trend("login_flow_ms");
const loginReqMsTrend = new Trend("login_req_ms");

export const options = {
  scenarios: {
    login:
      loadMode === "one-shot"
        ? {
            executor: "per-vu-iterations",
            vus: loadVus,
            iterations: loadIterations,
            maxDuration: loadDuration,
          }
        : {
            executor: "constant-vus",
            vus: loadVus,
            duration: loadDuration,
          },
  },
};

function buildEmail(vu) {
  const seq = String(vu).padStart(4, "0");
  return `${emailPrefix}${seq}@${emailDomain}`;
}

function getQueueStatus(token) {
  return http.get(`${baseUrl}/api/login/queue?token=${encodeURIComponent(token)}`);
}

function waitForLoginTurn() {
  const joinRes = http.post(`${baseUrl}/api/login/queue`, JSON.stringify({}), {
    headers: { "Content-Type": "application/json" },
  });

  check(joinRes, {
    "queue join 200": (r) => r.status === 200,
  });

  if (joinRes.status !== 200) {
    return { ok: false, queueToken: null, reason: "join_failed" };
  }

  const joinData = joinRes.json() || {};
  const queueToken = joinData.queueToken || null;
  if (joinData.allowed) {
    return { ok: true, queueToken, reason: "allowed" };
  }

  if (!queueToken) {
    return { ok: false, queueToken: null, reason: "token_missing" };
  }

  const startTime = Date.now();
  while (Date.now() - startTime < queueMaxWaitMs) {
    sleep(queuePollIntervalMs / 1000);
    const statusRes = getQueueStatus(queueToken);
    if (statusRes.status !== 200) {
      return { ok: false, queueToken, reason: "status_failed" };
    }
    const statusData = statusRes.json() || {};
    if (statusData.allowed) {
      return { ok: true, queueToken, reason: "allowed" };
    }
    if (statusData.expired) {
      return { ok: false, queueToken, reason: "expired" };
    }
  }

  return { ok: false, queueToken, reason: "timeout" };
}

export default function () {
  const flowStart = Date.now();
  let queueToken = null;
  if (useLoginQueue) {
    const queueStart = Date.now();
    const queueResult = waitForLoginTurn();
    if (!queueResult.ok) {
      const reason = queueResult.reason || "unknown";
      check({ reason }, {
        "queue allowed": (r) => r.reason === "allowed",
        "queue join failed": (r) => r.reason === "join_failed",
        "queue token missing": (r) => r.reason === "token_missing",
        "queue status failed": (r) => r.reason === "status_failed",
        "queue expired": (r) => r.reason === "expired",
        "queue timeout": (r) => r.reason === "timeout",
      });
      return;
    }
    queueToken = queueResult.queueToken;
    check({ reason: queueResult.reason }, {
      "queue allowed": (r) => r.reason === "allowed",
    });
    queueWaitMsTrend.add(Date.now() - queueStart);
  } else {
    queueWaitMsTrend.add(0);
  }

  const payload = {
    email: buildEmail(__VU),
    password,
    userType: "USER",
  };
  if (queueToken) {
    payload.queueToken = queueToken;
  }

  const loginPayload = JSON.stringify(payload);

  const loginStart = Date.now();
  const res = http.post(`${baseUrl}/api/login`, loginPayload, {
    headers: { "Content-Type": "application/json" },
  });
  loginReqMsTrend.add(Date.now() - loginStart);
  loginFlowMsTrend.add(Date.now() - flowStart);

  check(res, {
    "login status 200": (r) => r.status === 200,
  });
}

export function handleSummary(data) {
  const checks = data.root_group?.checks || [];
  const pickRate = (name) => {
    const entry = checks.find((check) => check.name === name);
    if (!entry) return null;
    const passes = entry.passes || 0;
    const fails = entry.fails || 0;
    const total = passes + fails;
    if (!total) return null;
    return ((passes / total) * 100).toFixed(2);
  };

  const summary = {
    queue_allowed_rate: pickRate("queue allowed"),
    queue_timeout_rate: pickRate("queue timeout"),
    queue_expired_rate: pickRate("queue expired"),
    queue_join_failed_rate: pickRate("queue join failed"),
    queue_token_missing_rate: pickRate("queue token missing"),
    queue_status_failed_rate: pickRate("queue status failed"),
  };

  const metrics = data.metrics || {};
  const duration = metrics.http_req_duration?.values || {};
  const failed = metrics.http_req_failed?.values || {};
  const iterations = metrics.iterations?.values || {};
  const dropped = metrics.dropped_iterations?.values || {};
  const httpReqs = metrics.http_reqs?.values || {};
  const queueWait = metrics.queue_wait_ms?.values || {};
  const loginFlow = metrics.login_flow_ms?.values || {};
  const loginReq = metrics.login_req_ms?.values || {};

  const formatNumber = (value, digits = 2) => {
    if (!Number.isFinite(value)) return "n/a";
    return value.toFixed(digits);
  };

  const summaryLines = [
    "TOTAL RESULTS (custom)",
    `http_req_duration: avg=${formatNumber(duration.avg)}ms min=${formatNumber(
      duration.min
    )}ms med=${formatNumber(duration.med)}ms max=${formatNumber(
      duration.max
    )}ms p(90)=${formatNumber(duration["p(90)"])}ms p(95)=${formatNumber(
      duration["p(95)"]
    )}ms`,
    `http_req_failed: rate=${formatNumber(failed.rate, 4)}`,
    `queue_wait_ms: avg=${formatNumber(queueWait.avg)}ms p(95)=${formatNumber(
      queueWait["p(95)"]
    )}ms`,
    `login_flow_ms: avg=${formatNumber(loginFlow.avg)}ms p(95)=${formatNumber(
      loginFlow["p(95)"]
    )}ms`,
    `login_req_ms: avg=${formatNumber(loginReq.avg)}ms p(95)=${formatNumber(
      loginReq["p(95)"]
    )}ms`,
    `iterations: count=${formatNumber(iterations.count, 0)} rate=${formatNumber(
      iterations.rate
    )}/s`,
    `dropped_iterations: count=${formatNumber(
      dropped.count,
      0
    )} rate=${formatNumber(dropped.rate)}/s`,
    `http_reqs: count=${formatNumber(httpReqs.count, 0)} rate=${formatNumber(
      httpReqs.rate
    )}/s`,
    "",
    "QUEUE SUMMARY",
    JSON.stringify(summary, null, 2),
  ];

  return {
    stdout: `${summaryLines.join("\n")}\n`,
  };
}
