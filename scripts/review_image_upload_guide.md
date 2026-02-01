# 리뷰 이미지 업로드 + SQL 시드 가이드

이 문서는 리뷰 이미지 오브젝트 스토리지 업로드와 `review_images_seed.sql` 생성 과정을 자동화한 스크립트 흐름과 동작 원리를 정리합니다.

🛠 무엇을 해결하나요?
기존에는 테스트용 이미지를 일일이 업로드하고 DB에 넣어야 했는데, 이제는 스크립트 하나로 오브젝트 스토리지 업로드 + SQL 생성까지 한 번에 처리됩니다.
✨ 핵심 기능
이미지 재사용: 소스 이미지가 5장만 있어도, 랜덤 조합하여 리뷰 600개 분량의 데이터를 만들 수 있습니다.
Manifest(매니페스트) 구조: 업로드 계획(CSV)과 실행 단계를 분리하여, 중간에 실패해도 어디까지 진행됐는지 확인 가능합니다.

## 개요

사용하는 스크립트:

- `scripts/batch_upload_review_images.sh` (권장 플로우)
- `scripts/upload_review_images.sh`
- `scripts/generate_review_image_manifest.sh` (선택)
- `scripts/split_sample_images.sh` (레거시, 선택)

현재 권장 플로우는 `.sample_images`에 있는 이미지를 재사용하여 리뷰당 1~3장을 랜덤으로 매칭합니다.

## 요약 표

| 항목              | 설명                                                     |
| ----------------- | -------------------------------------------------------- |
| 목적              | 리뷰 이미지 업로드 및 `review_images_seed.sql` 자동 생성 |
| 입력              | `.sample_images` 하위 이미지 파일                        |
| 출력              | `review_images_seed.sql`, `scripts/manifest.csv`         |
| 기본 이미지 수    | 리뷰당 1~3장                                             |
| 주요 스크립트     | `scripts/batch_upload_review_images.sh`                  |
| 업로드 엔드포인트 | `POST /api/v1/images/upload/reviews`                     |
| 필수 도구         | `curl`, `jq`, `python3`                                  |
| 선택 환경 변수    | `API_BASE_URL`, `OBJECT_STORAGE_BASE_URL`                |

## 실행 예시 표 (노션 공유용)

| 목적                        | 명령어                                                                                                              |
| --------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| ✅ 배치 업로드 + SQL 생성   | `scripts/batch_upload_review_images.sh --src .sample_images --count 600 --start-id 1 --min-images 1 --max-images 3` |
| ✅ 매니페스트 생성 (리뷰 1) | `scripts/generate_review_image_manifest.sh --dir .sample_images/review1 --review-id 1 --out scripts/manifest.csv`   |
| ✅ 매니페스트 기반 업로드   | `scripts/upload_review_images.sh --manifest scripts/manifest.csv --out review_images_seed.sql`                      |
| ✅ SQL 적용                 | `mysql -u root -p lunchgo < review_images_seed.sql`                                                                 |

## 실행 순서 흐름도

```
🗂️ .sample_images 준비
        ↓
🧾 manifest.csv 생성
        ↓
☁️ 오브젝트 스토리지 업로드
        ↓
🧩 review_images_seed.sql 생성
        ↓
🗄️ DB에 SQL 적용
```

```
✅ 권장: 배치 업로드 1줄 실행
   scripts/batch_upload_review_images.sh --src .sample_images --count 600 --start-id 1 --min-images 1 --max-images 3
```

```
✅ 대안: 수동 단계 실행
   1) scripts/generate_review_image_manifest.sh --dir .sample_images/review1 --review-id 1 --out scripts/manifest.csv
   2) scripts/upload_review_images.sh --manifest scripts/manifest.csv --out review_images_seed.sql
   3) mysql -u root -p lunchgo < review_images_seed.sql
```

## 사전 준비

- 업로드 API 서버 실행: `POST /api/v1/images/upload/reviews`
- `jq` 설치 (업로드 응답 파싱)
- `curl` 설치
- `python3` 설치 (`shuf`가 없는 환경에서 랜덤 샘플링용)

선택 환경 변수:

- `API_BASE_URL` (기본값: `http://localhost:8080`)
- `OBJECT_STORAGE_BASE_URL` (API가 `key`만 반환할 때 사용)

## 1) 소스 이미지 준비

`.sample_images` 아래에 이미지 파일을 둡니다. 하위 폴더 구조는 자유입니다.

예시:

```
.sample_images/food1.jpg
.sample_images/food2.png
.sample_images/dining/scene1.jpg
```

## 2) 배치 업로드 + SQL 생성

아래 스크립트가 매니페스트 생성 → 업로드 → SQL 생성까지 자동으로 수행합니다.

```
scripts/batch_upload_review_images.sh \
  --src .sample_images \
  --count 600 \
  --start-id 1 \
  --min-images 1 \
  --max-images 3
```

인자 설명:

- `--src` 이미지 소스 디렉터리
- `--count` 이미지가 연결될 리뷰 개수
- `--start-id` 리뷰 시작 ID (기본값: 1)
- `--min-images` 리뷰당 최소 이미지 수 (기본값: 1)
- `--max-images` 리뷰당 최대 이미지 수 (기본값: 3)
- `--out` 출력 SQL 파일 (기본값: `review_images_seed.sql`)

결과물:

- `review_images_seed.sql` (`review_images` 테이블 INSERT)
- `scripts/manifest.csv` (업로드에 사용한 임시 매니페스트)

## 3) SQL 적용

```
mysql -u root -p lunchgo < review_images_seed.sql
```

## (선택) 수동 매니페스트 생성

리뷰별 폴더 구조가 이미 있다면 매니페스트를 직접 만들 수 있습니다.

```
scripts/generate_review_image_manifest.sh --dir .sample_images/review1 --review-id 1 --out scripts/manifest.csv
scripts/upload_review_images.sh --manifest scripts/manifest.csv --out review_images_seed.sql
```

## 스크립트 동작 원리

### 1. `scripts/batch_upload_review_images.sh`

역할:

- `.sample_images`에서 이미지 목록 수집
- 리뷰마다 1~3장의 이미지를 랜덤으로 재사용하여 매니페스트 생성
- 매니페스트 기반 업로드 및 SQL 생성

코드:

```bash
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
```

핵심 포인트:

- 이미지 목록을 전부 수집한 뒤, 리뷰마다 1~3장을 랜덤 샘플링
- 샘플링은 `shuf`가 있으면 사용하고, 없으면 `python3`로 대체
- `manifest.csv`를 만든 뒤, 업로드/SQL 생성은 `upload_review_images.sh`에 위임

### 2. `scripts/upload_review_images.sh`

역할:

- 이미지 업로드 API 호출
- 응답의 `fileUrl` 또는 `key`로 URL 생성
- `review_images` INSERT SQL 작성

코드:

```bash
#!/usr/bin/env bash
set -euo pipefail

API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
OUT_FILE="review_images_seed.sql"
MODE=""
MANIFEST=""
IMAGE_DIR=""
REVIEW_ID=""
BASE_URL="${OBJECT_STORAGE_BASE_URL:-}"

usage() {
  cat <<'USAGE'
Usage:
  scripts/upload_review_images.sh --manifest <csv> [--out <file>]
  scripts/upload_review_images.sh --dir <image_dir> --review-id <id> [--out <file>]

Manifest format (CSV, no header):
  review_id,filepath,sort_order

Env:
  API_BASE_URL (default: http://localhost:8080)
  OBJECT_STORAGE_BASE_URL (optional; used if API response doesn't return fileUrl)
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --manifest)
      MODE="manifest"
      MANIFEST="$2"
      shift 2
      ;;
    --dir)
      MODE="dir"
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

if [[ -z "$MODE" ]]; then
  echo "Either --manifest or --dir is required." >&2
  usage
  exit 1
fi

if [[ "$MODE" == "manifest" && -z "$MANIFEST" ]]; then
  echo "--manifest requires a CSV path." >&2
  exit 1
fi

if [[ "$MODE" == "dir" ]]; then
  if [[ -z "$IMAGE_DIR" || -z "$REVIEW_ID" ]]; then
    echo "--dir mode requires --review-id." >&2
    exit 1
  fi
  if [[ ! -d "$IMAGE_DIR" ]]; then
    echo "Image directory not found: $IMAGE_DIR" >&2
    exit 1
  fi
fi

upload_file() {
  local file_path="$1"
  local response
  response=$(curl -sS -X POST \
    -F "file=@${file_path}" \
    "${API_BASE_URL}/api/v1/images/upload/reviews")

  local file_url
  local key
  file_url=$(echo "$response" | jq -r '.data.fileUrl // .fileUrl // empty')
  key=$(echo "$response" | jq -r '.data.key // .key // empty')

  if [[ -z "$file_url" && -n "$key" && -n "$BASE_URL" ]]; then
    file_url="${BASE_URL%/}/${key}"
  fi

  if [[ -z "$file_url" ]]; then
    echo "Upload failed or fileUrl missing for ${file_path}" >&2
    echo "Response: $response" >&2
    exit 1
  fi

  echo "$file_url"
}

echo "-- generated by scripts/upload_review_images.sh" > "$OUT_FILE"

if [[ "$MODE" == "manifest" ]]; then
  if [[ ! -f "$MANIFEST" ]]; then
    echo "Manifest file not found: $MANIFEST" >&2
    exit 1
  fi

  while IFS=, read -r review_id file_path sort_order; do
    [[ -z "${review_id// }" ]] && continue
    [[ "${review_id:0:1}" == "#" ]] && continue

    file_path="${file_path//\"/}"
    file_url=$(upload_file "$file_path")
    safe_url=$(printf "%s" "$file_url" | sed "s/'/''/g")

    echo "INSERT INTO review_images (review_id, image_url, sort_order) VALUES (${review_id}, '${safe_url}', ${sort_order});" >> "$OUT_FILE"
  done < "$MANIFEST"
else
  sort_order=0
  found_any=0
  while IFS= read -r file_path; do
    found_any=1
    file_url=$(upload_file "$file_path")
    safe_url=$(printf "%s" "$file_url" | sed "s/'/''/g")
    echo "INSERT INTO review_images (review_id, image_url, sort_order) VALUES (${REVIEW_ID}, '${safe_url}', ${sort_order});" >> "$OUT_FILE"
    sort_order=$((sort_order + 1))
  done < <(find "$IMAGE_DIR" -type f \( -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.png" -o -iname "*.webp" \) | sort)

  if [[ "$found_any" -eq 0 ]]; then
    echo "No images found in: $IMAGE_DIR" >&2
    exit 1
  fi
fi

echo "SQL saved to ${OUT_FILE}"
```

핵심 포인트:

- 업로드는 `curl`로 멀티파트 전송
- 응답 JSON에서 `fileUrl`을 우선 사용하고, 없다면 `key + OBJECT_STORAGE_BASE_URL`로 보정
- SQL은 바로 `review_images_seed.sql`에 누적 기록

### 3. `scripts/generate_review_image_manifest.sh`

역할:

- 특정 폴더의 이미지들을 리뷰 ID에 매핑하는 매니페스트 생성

코드:

```bash
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
```

핵심 포인트:

- 폴더 내부의 이미지 파일을 정렬하여 `sort_order`를 부여
- 결과는 `review_id,filepath,sort_order` 형식의 CSV

### 4. `scripts/split_sample_images.sh` (레거시)

역할:

- 샘플 이미지들을 `review1`, `review2` 같은 폴더로 랜덤 분배

코드:

```bash
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
```

핵심 포인트:

- 최상위 이미지 파일만 대상으로 하며, 파일을 실제로 이동시킴
- 현재는 이미지 재사용 전략 때문에 권장되지 않음

## 문제 해결

- `No images available for sampling.`
  - `.sample_images` 경로에 이미지가 없거나, 파일 확장자가 대상이 아님
- `jq: command not found`
  - `jq` 설치 필요
- 업로드 응답에 `fileUrl`이 없음
  - `OBJECT_STORAGE_BASE_URL`을 설정하거나 API 응답 포맷 확인
