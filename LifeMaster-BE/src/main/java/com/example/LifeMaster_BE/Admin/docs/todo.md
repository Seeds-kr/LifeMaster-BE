# LifeMaster-BE 관리자 API 남은 개발 내용

## 2단계: 통계/분석 API

### 회원 통계
- [ ] GET `/admin/stats/users/daily` - 일별 가입자 수
- [ ] GET `/admin/stats/users/monthly` - 월별 가입자 수
- [ ] GET `/admin/stats/users/retention` - 회원 유지율
- [ ] GET `/admin/stats/users/login-type` - 로그인 타입별 통계

### 콘텐츠 통계
- [ ] GET `/admin/stats/posts/daily` - 일별 게시글 수
- [ ] GET `/admin/stats/posts/type` - 게시글 타입별 통계
- [ ] GET `/admin/stats/comments/daily` - 일별 댓글 수
- [ ] GET `/admin/stats/popular-posts` - 인기 게시글 TOP 10

### 활동 통계
- [ ] GET `/admin/stats/active-users` - 활성 사용자 통계
- [ ] GET `/admin/stats/challenge/completion` - 챌린지 완료율

---

## 3단계: 시스템 설정 API

### 앱 설정
- [ ] GET `/admin/settings/app` - 앱 설정 조회
- [ ] PUT `/admin/settings/app` - 앱 설정 수정
- [ ] 공지사항 관리
- [ ] 약관 관리

### 결제 설정
- [ ] GET `/admin/settings/payment` - 결제 설정 조회
- [ ] PUT `/admin/settings/payment` - 결제 설정 수정
- [ ] 구독 플랜 관리

---

## 4단계: 로그/감사 기능

### 관리자 활동 로그
- [ ] AdminActionLog 엔티티 생성
- [ ] GET `/admin/logs/actions` - 관리자 활동 로그 조회
- [ ] 로그인, 회원 처리, 콘텐츠 삭제 등 기록

### 시스템 로그
- [ ] GET `/admin/logs/system` - 시스템 로그 조회
- [ ] 에러 로그 모니터링

---

## 5단계: 그룹/챌린지 관리

### 그룹 관리
- [ ] GET `/admin/groups` - 그룹 목록
- [ ] GET `/admin/groups/{groupId}` - 그룹 상세
- [ ] DELETE `/admin/groups/{groupId}` - 그룹 삭제
- [ ] GET `/admin/groups/{groupId}/members` - 그룹 멤버 목록

### 챌린지 관리
- [ ] GET `/admin/challenges` - 챌린지 목록
- [ ] GET `/admin/challenges/{challengeId}` - 챌린지 상세
- [ ] PATCH `/admin/challenges/{challengeId}` - 챌린지 수정
- [ ] DELETE `/admin/challenges/{challengeId}` - 챌린지 삭제

---

## 6단계: 결제/구독 관리

### 결제 내역
- [ ] GET `/admin/payments` - 결제 내역 목록
- [ ] GET `/admin/payments/{paymentId}` - 결제 상세
- [ ] POST `/admin/payments/{paymentId}/refund` - 환불 처리

### 구독 관리
- [ ] GET `/admin/subscriptions` - 구독 현황
- [ ] PATCH `/admin/users/{userId}/subscription` - 구독 수동 변경

---

## 7단계: 알림/푸시 관리

### 공지 알림
- [ ] POST `/admin/notifications/broadcast` - 전체 알림 발송
- [ ] GET `/admin/notifications/history` - 알림 발송 이력
- [ ] POST `/admin/notifications/users/{userId}` - 개별 알림 발송

---

## 8단계: 배치 작업 관리

### 스케줄러
- [ ] GET `/admin/jobs` - 배치 작업 목록
- [ ] POST `/admin/jobs/{jobId}/run` - 수동 실행
- [ ] PATCH `/admin/jobs/{jobId}/schedule` - 스케줄 변경

### 데이터 정리
- [ ] POST `/admin/maintenance/cleanup-inactive` - 비활성 계정 정리
- [ ] POST `/admin/maintenance/cleanup-reports` - 오래된 신고 정리

---

## 우선순위

| 순위 | 단계 | 중요도 | 비고 |
|-----|------|--------|------|
| 1 | 2단계 (통계) | 높음 | 대시보드 고도화에 필요 |
| 2 | 5단계 (그룹/챌린지) | 높음 | 핵심 기능 관리 |
| 3 | 6단계 (결제) | 높음 | 수익 관련 |
| 4 | 4단계 (로그) | 중간 | 운영 안정성 |
| 5 | 7단계 (알림) | 중간 | 사용자 소통 |
| 6 | 3단계 (설정) | 낮음 | 초기엔 DB 직접 수정 |
| 7 | 8단계 (배치) | 낮음 | 서비스 성장 후 |

---

## 추가 고려사항

### 보안
- [ ] 관리자 2단계 인증
- [ ] IP 화이트리스트
- [ ] 세션 타임아웃 설정

### 성능
- [ ] 통계 쿼리 최적화 (캐싱)
- [ ] 대량 데이터 엑셀 다운로드
- [ ] 페이지네이션 커서 기반 전환

### UI/UX
- [ ] 관리자 웹 프론트엔드 개발
- [ ] 실시간 대시보드 (WebSocket)
