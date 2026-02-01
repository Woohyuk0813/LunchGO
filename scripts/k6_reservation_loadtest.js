import http from "k6/http";
import { check, sleep } from "k6";

const baseUrl = __ENV.BASE_URL || "http://10.0.2.6:8080";
const emailPrefix = __ENV.EMAIL_PREFIX || "loadtest.user";
const emailDomain = __ENV.EMAIL_DOMAIN || "example.com";
const password = __ENV.PASSWORD || "Passw0rd!123";

const restaurantId = Number(__ENV.RESTAURANT_ID || 4);
const slotDate = __ENV.SLOT_DATE || "2026-01-16";
const slotTime = __ENV.SLOT_TIME || "12:00";
const partySize = Number(__ENV.PARTY_SIZE || 4);
const reservationType = __ENV.RESERVATION_TYPE || "RESERVATION_DEPOSIT";

const tokenByVu = {};
const userIdByVu = {};

const loadRate = Number(__ENV.LOAD_RATE || 100);
const loadVus = Number(__ENV.LOAD_VUS || 100);
const loadMaxVus = Number(__ENV.LOAD_MAX_VUS || Math.max(loadVus * 2, 50));
const loadDuration = __ENV.LOAD_DURATION || "1m";

export const options = {
  scenarios: {
    reservations: {
      executor: "constant-arrival-rate",
      rate: loadRate,
      timeUnit: "1s",
      duration: loadDuration,
      preAllocatedVUs: loadVus,
      maxVUs: loadMaxVus,
    },
  },
};

function buildEmail(vu) {
  const seq = String(vu).padStart(4, "0");
  return `${emailPrefix}${seq}@${emailDomain}`;
}

function login(vu) {
  const payload = JSON.stringify({
    email: buildEmail(vu),
    password,
    userType: "USER",
  });

  const res = http.post(`${baseUrl}/api/login`, payload, {
    headers: { "Content-Type": "application/json" },
  });

  check(res, {
    "login status 200": (r) => r.status === 200,
  });

  if (res.status !== 200) {
    return null;
  }

  const body = res.json();
  return {
    token: body.accessToken,
    userId: body.id,
  };
}

export default function () {
  const vu = __VU;
  if (!tokenByVu[vu]) {
    const creds = login(vu);
    if (!creds) {
      sleep(1);
      return;
    }
    tokenByVu[vu] = creds.token;
    userIdByVu[vu] = creds.userId;
  }

  const payload = JSON.stringify({
    userId: userIdByVu[vu],
    restaurantId,
    slotDate,
    slotTime,
    partySize,
    reservationType,
    requestMessage: null,
  });

  const res = http.post(`${baseUrl}/api/reservations`, payload, {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${tokenByVu[vu]}`,
    },
  });

  check(res, {
    "reservation status 2xx/4xx": (r) => r.status >= 200 && r.status < 500,
  });

  sleep(0.1);
}
