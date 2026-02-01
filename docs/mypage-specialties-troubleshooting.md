# 마이페이지 특이사항 드롭다운 트러블슈팅 기록

## 문제 1: 특이사항 드롭다운 목록이 카드 밖으로 안 나옴

### 증상
- 마이페이지 특이사항 드롭다운이 카드 내부에서 잘려 전체 목록이 보이지 않음.

### 원인
- 카드 컴포넌트(`.info-card`)에 `overflow: hidden`이 기본 적용되어 드롭다운이 잘림.
- 드롭다운이 포함된 섹션이 동일한 카드 스타일을 그대로 사용.

### 해결
- 특이사항 카드에만 `overflow: visible`을 적용해 드롭다운이 카드 밖으로 펼쳐지도록 처리.
- 드롭다운 래퍼에 `z-index` 보정 적용.

### 해결 과정
- 카드 공통 스타일에서 `overflow: hidden` 확인.
- 특이사항 섹션에만 예외 스타일을 적용하도록 클래스 분리.
- 카드 외부로 드롭다운이 정상적으로 펼쳐지는지 확인.

### 적용 코드

```vue
<!-- 특이사항 섹션 카드에만 overflow 예외 적용 -->
<div class="info-card info-card--overflow">
  <div class="card-title">기타</div>
  <div class="p-6 space-y-5 interest-wrap">
    <!-- 특이사항 드롭다운 영역 -->
  </div>
</div>
```

```css
/* 기본 카드 스타일은 overflow: hidden 유지 */
.info-card {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #e9ecef;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

/* 드롭다운이 들어가는 카드에만 예외 적용 */
.info-card--overflow {
  overflow: visible;
}

/* 드롭다운을 위 레이어로 올려 겹침 방지 */
.interest-wrap {
  position: relative;
  z-index: 20;
}
```

### 적용 파일
- `frontend/src/views/mypage/UserMyPage.vue`

---

## 문제 2: 특이사항 드롭다운 길이가 너무 길어 페이지 밖으로 내려감

### 증상
- 선택지 수가 많아 드롭다운이 페이지 하단 밖으로 길게 펼쳐짐.

### 원인
- 드롭다운 높이에 제한이 없어 전체 옵션이 그대로 렌더링됨.

### 해결
- 뷰포트 높이에 따라 동적으로 최대 높이를 조절하고 스크롤로 표시.
- 최대 높이는 40% 기준, 최소 180px~최대 320px 범위로 제한.

### 해결 과정
- 고정 높이 적용 후 화면 크기에 따라 다시 조정 필요 확인.
- `window.innerHeight` 기반 동적 계산 함수 추가.
- 드롭다운 렌더링 시 `style` 바인딩으로 최대 높이 반영.

### 적용 코드

```ts
// 화면 높이에 맞게 드롭다운 최대 높이를 계산
const getInterestDropdownStyle = () => {
  const viewportHeight = window.innerHeight || 0;
  const maxHeight = Math.min(320, Math.max(180, Math.round(viewportHeight * 0.4)));
  return {
    maxHeight: `${maxHeight}px`,
  };
};
```

```vue
<!-- 드롭다운에 동적 최대 높이 적용 -->
<div
  v-if="openInterestIndex === index"
  class="absolute left-0 right-0 mt-2 bg-white border border-[#e9ecef] rounded-lg shadow-md z-30 overflow-y-auto"
  :style="getInterestDropdownStyle()"
>
  <!-- 옵션 목록 -->
</div>
```

### 적용 파일
- `frontend/src/views/mypage/UserMyPage.vue`
