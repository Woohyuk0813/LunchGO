#!/usr/bin/env bash
set -euo pipefail

OUT_FILE="manifest.csv"
IMAGE_DIR=""
REVIEW_ID=""

usage() {
  cat <<'USAGE'
Usage:
  scripts/generate_review_image_manifest.sh --dir <image_dir> --review-id <id> [--out <file>]

Output format (CSV, no header):
  review_id,filepath,sort_order
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dir)
      IMAGE_DIR="$2"
      shift 2
      ;;
    --review-id)
      REVIEW_ID="$2"
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

if [[ -z "$IMAGE_DIR" || -z "$REVIEW_ID" ]]; then
  echo "--dir and --review-id are required." >&2
  usage
  exit 1
fi

if [[ ! -d "$IMAGE_DIR" ]]; then
  echo "Image directory not found: $IMAGE_DIR" >&2
  exit 1
fi

echo "# review_id,filepath,sort_order" > "$OUT_FILE"
sort_order=0
found_any=0
while IFS= read -r file_path; do
  found_any=1
  printf "%s,%s,%s\n" "$REVIEW_ID" "$file_path" "$sort_order" >> "$OUT_FILE"
  sort_order=$((sort_order + 1))
done < <(find "$IMAGE_DIR" -type f \( -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.png" -o -iname "*.webp" \) | sort)

if [[ "$found_any" -eq 0 ]]; then
  echo "No images found in: $IMAGE_DIR" >&2
  exit 1
fi

echo "Manifest saved to ${OUT_FILE}"
