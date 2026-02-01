import http from "k6/http";
import { check, sleep } from "k6";

const baseUrl = __ENV.BASE_URL || "http://10.0.2.6:8080";
const emailPrefix = __ENV.EMAIL_PREFIX || "loadtest.user";
const emailDomain = __ENV.EMAIL_DOMAIN || "example.com";
const password = __ENV.PASSWORD || "Passw0rd!123";
const useLoginQueue =
  String(__ENV.USE_LOGIN_QUEUE || "").toLowerCase() === "true" ||
  String(__ENV.USE_LOGIN_QUEUE || "") === "1";
const queuePollIntervalMs = Number(__ENV.LOGIN_QUEUE_POLL_MS || 1000);
const queueMaxWaitMs = Number(__ENV.LOGIN_QUEUE_MAX_WAIT_MS || 60000);

const restaurantId = Number(__ENV.RESTAURANT_ID || 4);
const slotDate = __ENV.SLOT_DATE || "2026-01-16";
const slotTime = __ENV.SLOT_TIME || "12:00";
const partySize = Number(__ENV.PARTY_SIZE || 4);
const reservationType = __ENV.RESERVATION_TYPE || "RESERVATION_DEPOSIT";

const loadVus = Number(__ENV.LOAD_VUS || 10);
const loadDuration = __ENV.LOAD_DURATION || "30s";

export const options = {
  scenarios: {
    reservations: {
      executor: "per-vu-iterations",
      vus: loadVus,
      iterations: 1,
      maxDuration: loadDuration,
    },
  },
};

function buildEmail(vu) {
  const seq = String(vu).padStart(4, "0");
  return `${emailPrefix}${seq}@${emailDomain}`;
}

const cachedAuth = { token: null, userId: null };

function waitForLoginTurn() {
  const joinRes = http.post(`${baseUrl}/api/login/queue`, JSON.stringify({}), {
    headers: { "Content-Type": "application/json" },
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
    const statusRes = http.get(
      `${baseUrl}/api/login/queue?token=${encodeURIComponent(queueToken)}`
    );
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

function loginUser(email) {
  let queueToken = null;
  if (useLoginQueue) {
    const queueResult = waitForLoginTurn();
    if (!queueResult.ok) {
      console.error(
        `Queue failed for user ${email}: ${queueResult.reason || "unknown"}`
      );
      return null;
    }
    queueToken = queueResult.queueToken;
  }

  const loginPayload = JSON.stringify({
    email,
    password,
    userType: "USER",
    ...(queueToken ? { queueToken } : {}),
  });

  const loginRes = http.post(`${baseUrl}/api/login`, loginPayload, {
    headers: { "Content-Type": "application/json" },
  });

  check(loginRes, {
    "login status 200": (r) => r.status === 200,
  });

  if (loginRes.status !== 200) {
    console.error(`Login failed for user ${email}: ${loginRes.status} ${loginRes.body}`);
    return null;
  }

  const loginBody = loginRes.json() || {};
  if (!loginBody.accessToken || !loginBody.id) {
    console.error(`Login response missing token for user ${email}`);
    return null;
  }

  return {
    token: loginBody.accessToken,
    userId: loginBody.id,
  };
}

function getCachedAuth() {
  if (cachedAuth.token && cachedAuth.userId) {
    return cachedAuth;
  }

  const email = buildEmail(__VU);
  const auth = loginUser(email);
  if (!auth) {
    return null;
  }

  cachedAuth.token = auth.token;
  cachedAuth.userId = auth.userId;
  return cachedAuth;
}

export default function () {
  const user = getCachedAuth();

  if (!user) {
    return;
  }

  const { token, userId } = user;

  const reservePayload = JSON.stringify({
    userId,
    restaurantId,
    slotDate,
    slotTime,
    partySize,
    reservationType,
    requestMessage: null,
  });

  const reserveRes = http.post(`${baseUrl}/api/reservations`, reservePayload, {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  check(reserveRes, {
    "reservation status 2xx/4xx": (r) => r.status >= 200 && r.status < 500,
  });
}
