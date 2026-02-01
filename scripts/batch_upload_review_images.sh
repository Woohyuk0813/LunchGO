#!/usr/bin/env bash
set -euo pipefail

SRC_DIR=""
COUNT=""
OUT_FILE="review_images_seed.sql"
BASE_URL="${API_BASE_URL:-http://localhost:8080}"
MIN_IMAGES=1
MAX_IMAGES=3
START_ID=1

usage() {
  cat <<'USAGE'
Usage:
  scripts/batch_upload_review_images.sh --src <dir> --count <N> [--start-id <id>] [--min-images <n>] [--max-images <n>] [--out <file>]

Steps:
  1) Build manifest by reusing images (1~3 per review by default)
  2) Upload images
  3) Generate SQL
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --src)
      SRC_DIR="$2"
      shift 2
      ;;
    --count)
      COUNT="$2"
      shift 2
      ;;
    --start-id)
      START_ID="$2"
      shift 2
      ;;
    --min-images)
      MIN_IMAGES="$2"
      shift 2
      ;;
    --max-images)
      MAX_IMAGES="$2"
      shift 2
      ;;
    --out)
      OUT_FILE="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ -z "$SRC_DIR" || -z "$COUNT" ]]; then
  echo "--src and --count are required." >&2
  usage
  exit 1
fi

if [[ ! -d "$SRC_DIR" ]]; then
  echo "Source directory not found: $SRC_DIR" >&2
  exit 1
fi

if ! [[ "$COUNT" =~ ^[0-9]+$ ]] || [[ "$COUNT" -lt 1 ]]; then
  echo "--count must be a positive integer." >&2
  exit 1
fi

if ! [[ "$START_ID" =~ ^[0-9]+$ ]] || [[ "$START_ID" -lt 1 ]]; then
  echo "--start-id must be a positive integer." >&2
  exit 1
fi

if ! [[ "$MIN_IMAGES" =~ ^[0-9]+$ ]] || [[ "$MIN_IMAGES" -lt 1 ]]; then
  echo "--min-images must be a positive integer." >&2
  exit 1
fi

if ! [[ "$MAX_IMAGES" =~ ^[0-9]+$ ]] || [[ "$MAX_IMAGES" -lt 1 ]]; then
  echo "--max-images must be a positive integer." >&2
  exit 1
fi

if [[ "$MIN_IMAGES" -gt "$MAX_IMAGES" ]]; then
  echo "--min-images cannot be greater than --max-images." >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MANIFEST="${SCRIPT_DIR}/manifest.csv"
rm -f "$MANIFEST"

images=()
while IFS= read -r file_path; do
  images+=("$file_path")
done < <(
  find "$SRC_DIR" -type f \( -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.png" -o -iname "*.webp" \) \
    | sort
)

if [[ "${#images[@]}" -eq 0 ]]; then
  echo "No images found in: $SRC_DIR" >&2
  exit 1
fi

echo "# review_id,filepath,sort_order" > "$MANIFEST"
total_images="${#images[@]}"
max_offset=$((MAX_IMAGES - MIN_IMAGES + 1))
SAMPLE_CMD=""

if command -v shuf >/dev/null 2>&1; then
  SAMPLE_CMD="shuf"
elif command -v python3 >/dev/null 2>&1; then
  SAMPLE_CMD="python3"
else
  echo "Either 'shuf' or 'python3' is required to sample images." >&2
  exit 1
fi

sample_unique() {
  local count="$1"
  if [[ "$SAMPLE_CMD" == "shuf" ]]; then
    shuf -n "$count"
  else
    python3 -c 'import random,sys
count=int(sys.argv[1])
items=[line.rstrip("\n") for line in sys.stdin if line.rstrip("\n")]
if not items:
    print("No images available for sampling.", file=sys.stderr)
    sys.exit(1)
if count <= len(items):
    random.shuffle(items)
    for item in items[:count]:
        print(item)
else:
    for _ in range(count):
        print(random.choice(items))
' "$count"
  fi
}

picked=()
for ((i = 0; i < COUNT; i++)); do
  review_id=$((START_ID + i))
  num_images=$(( (RANDOM % max_offset) + MIN_IMAGES ))

  picked=()
  if [[ "$num_images" -le "$total_images" ]]; then
    while IFS= read -r file_path; do
      picked+=("$file_path")
    done < <(printf "%s\n" "${images[@]}" | sample_unique "$num_images")
  else
    for ((j = 0; j < num_images; j++)); do
      picked+=("${images[RANDOM % total_images]}")
    done
  fi

  sort_order=0
  for file_path in "${picked[@]}"; do
    printf "%s,%s,%s\n" "$review_id" "$file_path" "$sort_order" >> "$MANIFEST"
    sort_order=$((sort_order + 1))
  done
done

API_BASE_URL="${BASE_URL}" \
"${SCRIPT_DIR}/upload_review_images.sh" --manifest "$MANIFEST" --out "$OUT_FILE"

echo "Done. SQL saved to ${OUT_FILE}"
