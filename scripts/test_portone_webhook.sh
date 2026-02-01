#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
WEBHOOK_SECRET="${PORTONE_WEBHOOK_SECRET:-}"
PAYMENT_ID="${PAYMENT_ID:-}"
STORE_ID="${STORE_ID:-store-test}"
TRANSACTION_ID="${TRANSACTION_ID:-tx-test}"
EVENT_TYPE="${EVENT_TYPE:-Transaction.Paid}"
TIMESTAMP_ISO="${TIMESTAMP_ISO:-2024-04-25T10:00:00.000Z}"

if [[ -z "$WEBHOOK_SECRET" ]]; then
  echo "PORTONE_WEBHOOK_SECRET is required." >&2
  exit 1
fi

if [[ -z "$PAYMENT_ID" ]]; then
  echo "PAYMENT_ID is required (merchantUid)." >&2
  exit 1
fi

WEBHOOK_ID="test-$(uuidgen)"
WEBHOOK_TS="$(date +%s)"

BODY=$(cat <<JSON
{"type":"${EVENT_TYPE}","timestamp":"${TIMESTAMP_ISO}","data":{"storeId":"${STORE_ID}","paymentId":"${PAYMENT_ID}","transactionId":"${TRANSACTION_ID}"}}
JSON
)
BODY="${BODY%$'\n'}"

SIGNING_PAYLOAD="${WEBHOOK_ID}.${WEBHOOK_TS}.${BODY}"

KEY_STRIPPED="${WEBHOOK_SECRET}"
if [[ "$KEY_STRIPPED" == whsec_* ]]; then
  KEY_STRIPPED="${KEY_STRIPPED#whsec_}"
fi

if decoded=$(printf "%s" "$KEY_STRIPPED" | base64 --decode 2>/dev/null); then
  KEY_HEX=$(printf "%s" "$KEY_STRIPPED" | base64 --decode | xxd -p -c 256)
  SIG=$(printf "%s" "$SIGNING_PAYLOAD" | openssl dgst -sha256 -mac HMAC -macopt hexkey:"$KEY_HEX" -binary | openssl base64)
elif decoded=$(printf "%s" "$KEY_STRIPPED" | base64 -D 2>/dev/null); then
  KEY_HEX=$(printf "%s" "$KEY_STRIPPED" | base64 -D | xxd -p -c 256)
  SIG=$(printf "%s" "$SIGNING_PAYLOAD" | openssl dgst -sha256 -mac HMAC -macopt hexkey:"$KEY_HEX" -binary | openssl base64)
else
  SIG=$(printf "%s" "$SIGNING_PAYLOAD" | openssl dgst -sha256 -hmac "$WEBHOOK_SECRET" -binary | openssl base64)
fi

curl -i -X POST "${BASE_URL}/api/payments/portone/webhook" \
  -H "Content-Type: application/json" \
  -H "webhook-id: ${WEBHOOK_ID}" \
  -H "webhook-timestamp: ${WEBHOOK_TS}" \
  -H "webhook-signature: v1,${SIG}" \
  -d "${BODY}"
