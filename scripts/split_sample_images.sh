#!/usr/bin/env bash
set -euo pipefail

SRC_DIR=""
COUNT=""
PREFIX="review"

usage() {
  cat <<'USAGE'
Usage:
  scripts/split_sample_images.sh --src <dir> --count <N> [--prefix review]

Example:
  scripts/split_sample_images.sh --src .sample_images --count 3
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
    --prefix)
      PREFIX="$2"
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

for ((i = 1; i <= COUNT; i++)); do
  mkdir -p "${SRC_DIR}/${PREFIX}${i}"
done

idx=0
found_any=0
while IFS= read -r file_path; do
  found_any=1
  folder_index=$(( (idx % COUNT) + 1 ))
  mv "$file_path" "${SRC_DIR}/${PREFIX}${folder_index}/"
  idx=$((idx + 1))
done < <(
  find "$SRC_DIR" -maxdepth 1 -type f \( -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.png" -o -iname "*.webp" \) \
  | sort -R
)

if [[ "$found_any" -eq 0 ]]; then
  echo "No images found in: $SRC_DIR" >&2
  exit 1
fi

echo "Split ${idx} images into ${COUNT} folders under ${SRC_DIR}"
