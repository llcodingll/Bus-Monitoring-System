# OnlineStatusPolicy 분리 및 경계값 수정

> 작업일: 2026-06-26
> 대상: `GET /api/buses` — ONLINE/OFFLINE 판정 로직

## 해결한 문제

### 1. 경계값 오류 (버그)

**PDF 요구사항:** "5분 초과 = OFFLINE, 5분 이하 = ONLINE"
→ "이하(≤)"이므로 정확히 5분 경과는 **ONLINE**이어야 한다.

**기존 코드:**
```java
lastCommunicatedAt.isAfter(now.minus(THRESHOLD))
```
`isAfter`는 exclusive 비교이므로 정확히 5분 경과 시 `false` → **OFFLINE** (오류)

**수정 후:**
```java
!lastCommunicatedAt.isBefore(now.minus(THRESHOLD))
```
`!isBefore`는 inclusive 비교이므로 정확히 5분 경과 시 `true` → **ONLINE** (정확)

### 2. 인라인 로직 분리

ONLINE/OFFLINE 판정이 `BusService` 내부에 인라인으로 존재해 단위 테스트가 불가능했다.
`OnlineStatusPolicy` 클래스로 분리해 Spring 컨텍스트 없이 순수 단위 테스트가 가능하도록 했다.

### 3. `Instant.now()` 단일화

기존에는 버스마다 `LocalDateTime.now()`를 호출해 요청 처리 중 시각이 미세하게 달라졌다.
`Instant.now()`를 요청 시작 시점에 한 번만 호출해 동일 요청 내 모든 버스에 동일한 기준 시각을 적용한다.

## 단위 테스트 (OnlineStatusPolicyTest)

`test_scenarios_mvp.md §1.1` 케이스 10개 전부 작성 및 통과:

| # | 조건 | 기대값 |
|---|------|--------|
| 1 | 0초 경과 | ONLINE |
| 2 | 4분 59초 | ONLINE |
| 3 | 정확히 5분 (경계) | ONLINE |
| 4 | 5분 + 1ms | OFFLINE |
| 5 | 5분 1초 | OFFLINE |
| 6 | 10분 | OFFLINE |
| 7 | 1일 경과 | OFFLINE |
| 8 | null (미통신) | OFFLINE |
| 9 | 시계 역전 (음수 경과) | ONLINE (예외 없음) |
| 10 | last == now (동일 시각) | ONLINE |

## 커밋 대상 파일

```
backend/src/main/java/.../api/bus/policy/OnlineStatusPolicy.java   [신규]
backend/src/main/java/.../api/bus/service/BusService.java
backend/src/test/java/.../api/bus/policy/OnlineStatusPolicyTest.java  [신규]
docs/OnlineStatusPolicy-분리-및-경계값-수정.md  [이 파일]
```

## 커밋 메시지

```
fix: ONLINE 판정 경계값 수정 (5분 이하 포함) 및 OnlineStatusPolicy 분리
```
