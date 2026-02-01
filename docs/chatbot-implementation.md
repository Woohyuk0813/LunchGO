# 🤖 Naver Clova Chatbot 구현

## 개요
- 네이버 클로바 챗봇 API와 연동하여 웹에서 사용자 CS 제공
- 프론트는 `/chatbot` 전용 페이지에서 채팅 UI 제공
- 백엔드는 `open/send` 이벤트 분리 호출 및 서명 처리

## 백엔드

### 1) 챗봇 API 연동 서비스
- 경로: `src/main/java/com/example/LunchGo/chatbot/service/NcpChatbotService.java`
- 역할
  - NCP 챗봇 요청 본문 생성 (`version`, `userId`, `timestamp`, `bubbles`, `event`)
  - `X-NCP-CHATBOT_SIGNATURE` 서명 생성
  - `open` 이벤트 / `send` 이벤트 분리 호출
  - `userIp` 전달 지원

### 2) 컨트롤러 추가
- 경로: `src/main/java/com/example/LunchGo/chatbot/controller/ChatbotController.java`
- 엔드포인트
  - `POST /api/chatbot/open`
    - open 이벤트 호출
    - 응답은 클로바 챗봇 원본 JSON 그대로 반환
  - `POST /api/chatbot/message`
    - send 이벤트 호출
    - 응답은 클로바 챗봇 원본 JSON 그대로 반환

### 3) 요청 DTO
- 경로: `src/main/java/com/example/LunchGo/chatbot/dto/ChatbotMessageRequest.java`
- 필드
  - `message`: 사용자가 입력한 메시지
  - `userId`: 클로바 챗봇 유저 식별값

### 4) 환경설정
- `application-secret.properties`
```
ncp.chatbot.api-url=YOUR_NCP_CHATBOT_API_URL
ncp.chatbot.secret-key=YOUR_NCP_CHATBOT_SECRET_KEY
```

## 프론트엔드

### 1) 전용 챗봇 페이지
- 경로: `frontend/src/views/chatbot/ChatBotPage.vue`
- 기능 요약
  - 로그인 상태/ROLE_USER 권한 체크
  - 페이지 진입 시 `/api/chatbot/open` 호출
  - 메시지 전송 시 `/api/chatbot/message` 호출
  - `text` / `template` 응답 타입 렌더링
  - 템플릿 버튼 클릭 시 postback 자동 전송
  - 타이핑 인디케이터(점 3개 애니메이션)
  - 새 메시지 도착 시 자동 스크롤

### 2) 라우터 등록
- 경로: `frontend/src/router/index.js`
- 추가 라우트
```
{
  path: '/chatbot',
  name: 'chatbot',
  component: () => import('../views/chatbot/ChatBotPage.vue'),
  meta: { requiredAuth: true, roles: ['ROLE_USER'] },
}
```

### 3) 홈 화면에서 챗봇 접근
- 경로: `frontend/src/views/HomeView.vue`
- 동작
  - 하단 네비게이션에서 챗봇 버튼 클릭 시 `/chatbot` 이동

### 4) 하단 네비게이션 버튼
- 경로: `frontend/src/components/ui/BottomNav.vue`
- 기능
  - 로그인된 경우만 챗봇 버튼 표시
  - 버튼 hover/active 시 “고객센터” 라벨 표시

## 동작 흐름

1. 사용자가 하단 챗봇 버튼 클릭
2. `/chatbot` 페이지 이동
3. 페이지 로드 시 `/api/chatbot/open` 호출 (open 이벤트)
4. 응답의 `bubbles`를 렌더링
5. 사용자 입력 → `/api/chatbot/message` 호출 (send 이벤트)
6. 응답 `bubbles`를 렌더링

## 응답 타입 처리
- `type: text`
  - `data.description` 그대로 출력
- `type: template`
  - `cover.data.description` 출력
  - `contentTable` 버튼 목록 렌더링
  - 버튼 클릭 시 `postback` 자동 전송

## 참고 사항
- `userId`는 프론트에서 생성해 로컬스토리지에 저장
  - 키: `chatbotUserId`
  - 매번 동일한 ID로 대화를 이어감
- 페이지를 나갔다가 재진입하면 메시지는 초기화됨
- open 응답이 실패하더라도 기본 안내 메시지를 표시
