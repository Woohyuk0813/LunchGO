#!/usr/bin/env bash
set -euo pipefail

# Upload frontend dist to NCP Object Storage (S3-compatible).
# Requirements: awscli installed and configured via env vars below.

DIST_DIR="${DIST_DIR:-frontend/dist}"
BUCKET="${BUCKET:-lunchgo-test-bucket}"
REGION="${REGION:-kr-standard}"
ENDPOINT_URL="${ENDPOINT_URL:-http://kr.object.ncloudstorage.com}"
ACL="${ACL:-}"
DRY_RUN="${DRY_RUN:-0}"

if ! command -v aws >/dev/null 2>&1; then
  echo "aws cli not found. Install awscli first."
  exit 1
fi

if [[ ! -d "${DIST_DIR}" ]]; then
  echo "dist directory not found: ${DIST_DIR}"
  exit 1
fi

SYNC_FLAGS=("--region" "${REGION}" "--endpoint-url" "${ENDPOINT_URL}")

if [[ "${DRY_RUN}" == "1" ]]; then
  SYNC_FLAGS+=("--dryrun")
fi

if [[ -n "${ACL}" ]]; then
  SYNC_FLAGS+=("--acl" "${ACL}")
fi

if [[ -z "${AWS_ACCESS_KEY_ID:-}" || -z "${AWS_SECRET_ACCESS_KEY:-}" ]]; then
  echo "AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY not set. Using default AWS credentials/profile."
fi

AWS_S3_FORCE_PATH_STYLE=true aws s3 sync "${DIST_DIR}" "s3://${BUCKET}" "${SYNC_FLAGS[@]}" --delete

echo "Upload complete."
