# 서울 버스 관제 시스템

현재 운행 중인 버스의 위치·상태·이벤트를 실시간으로 확인하는 웹 기반 관제 플랫폼입니다.

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Spring Boot 4.x · Java 17 · JPA · PostgreSQL 16 |
| Frontend | Vue 3 (Composition API) · TypeScript · Pinia · Tailwind CSS v4 · Leaflet |
| Infrastructure | Docker Compose |

---

## 실행 방법

**사전 조건**: Docker Desktop이 실행 중이어야 합니다.

```bash
# 1. 전체 서비스 빌드 및 기동
docker compose up --build

# 2. 초기 Mock 데이터 시드 (최초 1회, 또는 데이터 초기화 시)
curl -X POST http://localhost/api/seed
```

브라우저에서 `http://localhost` 에 접속합니다.

> 시드를 실행하면 버스 20대 · 노선 8개 · 정류장 36개가 생성되고, 7초 간격으로 위치 시뮬레이션이 자동 시작됩니다.

### 서비스 구성

| 서비스 | 주소 |
|--------|------|
| 관제 화면 | `http://localhost` |
| Backend API | `http://localhost/api` (Nginx 리버스 프록시) |
| PostgreSQL | `localhost:5432` (컨테이너 내부: busmonitoring / bususer) |

---

## 주요 기능

- **버스 목록**: 버스번호 · 노선명 · 현재속도 · ONLINE/OFFLINE 상태 실시간 표시
- **버스 상세**: 현재 정류장 · 다음 정류장 · 운행방향 · 마지막 통신시간
- **지도**: Leaflet 기반 버스 위치 마커 · 7초 주기 부드러운 애니메이션 · 최근 이동 경로 폴리라인
- **이벤트 목록**: 급정거 · 급가속 · 급감속 · 급출발 · 충격 감지 이벤트 실시간 조회 · 유형 및 심각도 필터링
- **온라인 상태 판정**: 마지막 통신시간 기준 5분 초과 → OFFLINE, 5분 이내 → ONLINE

### API 엔드포인트

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/buses` | 버스 목록 |
| GET | `/api/buses/{id}` | 버스 상세 |
| GET | `/api/buses/{id}/path` | 최근 GPS 이력 (최대 50건) |
| GET | `/api/buses/{id}/events` | 버스별 이벤트 목록 |
| GET | `/api/events` | 전체 이벤트 목록 (`?eventType=`, `?severity=` 필터 지원) |
| POST | `/api/seed` | Mock 데이터 초기화 |

---

## 기술 선택 이유

### PostgreSQL (vs Elasticsearch 등)

이 시스템의 조회 패턴은 버스 ID 또는 노선 ID 기준 **정확 매칭 + 시간 범위 필터**입니다. 전문 검색(형태소 분석, 유사도 랭킹)이 필요하지 않아 Elasticsearch 같은 검색 엔진은 오버스펙입니다. PostgreSQL의 복합 인덱스(`bus_id, recorded_at`)로 해당 조회 패턴을 충분히 커버하며, 트랜잭션 정합성과 JOIN 처리도 RDB가 자연스럽습니다.

### Vue 3 (vs React / Next.js)

여러 버스가 각자 독립적으로 위치를 갱신하는 이 도메인에서 Vue 3의 **Proxy 기반 반응성(속성 단위 의존성 추적)** 이 유리합니다. React는 상태 변경 시 리스트를 전체 재렌더링하고 `memo` / `useMemo` 를 직접 챙겨야 최적화되는 반면, Vue 3는 변경된 속성과 연결된 컴포넌트만 기본 동작으로 업데이트합니다. Mock 20대 규모에서는 차이가 체감되지 않지만, 실제 서비스(서울 7,000여 대)로 확장했을 때 이 반응성 모델 차이가 의미 있습니다.

### 갱신 주기 7초 (vs 1초)

1초마다 위치를 갱신하면 버스 7,000대 기준 초당 7,000건의 DB 쓰기가 발생합니다. 관제 화면에서 버스 위치의 실질적인 체감 정밀도는 5~10초 수준이면 충분하고, Leaflet 마커 애니메이션으로 시각적 연속성을 보완합니다. 7초는 시뮬레이터 tick 주기와 프론트엔드 폴링 주기를 일치시켜 구현을 단순하게 유지하는 값이기도 합니다.

### HTTP Polling (vs WebSocket) · Redis / 메시지 큐 미사용

WebSocket은 연결 수 관리, 재연결 로직, 스케일아웃 시 Sticky Session 또는 Redis Pub/Sub이 필요해 운영 복잡도가 높습니다. 현재 Mock 규모(20대, 단일 관제 화면)에서는 7초 간격 HTTP Polling으로 실시간성을 충분히 구현할 수 있습니다. Redis나 Kafka 같은 메시지 브로커는 수평 확장이 필요한 시점에 도입하는 것이 맞습니다—지금 규모에 넣으면 오버엔지니어링이며, "지금 풀어야 할 문제가 없는 솔루션"입니다. 확장 전략은 설계 문서의 §5 향후 확장 섹션에 정리했습니다.

### 인덱스 · N+1 방지 · 현재상태 비정규화

**인덱스**: `gps_locations`에 `(bus_id, recorded_at DESC)`, `bus_dispatches`에 `(operation_ended_at, bus_id, route_id, direction)` 복합 인덱스를 적용했습니다. `events` 테이블에는 조회 패턴별로 `(bus_id, occurred_at DESC)`, `(event_type, occurred_at DESC)`, `(severity, occurred_at DESC)` 인덱스 3종을 추가해 버스별·유형별·심각도별 필터 조회를 모두 인덱스 스캔으로 처리합니다.

**N+1 방지**: 버스 목록 조회(`GET /api/buses`)는 버스 전체와 현재·다음 정류장을 `LEFT JOIN FETCH` 단일 쿼리로 로드합니다. 버스 상세의 배차 정보는 `findActiveByBusId()` 단건 쿼리로 전체 로드 후 Java 필터링 방식을 교체했습니다.

**현재상태 비정규화**: `Bus` 엔티티에 `current_latitude`, `current_longitude`, `current_speed`, `current_stop_id`, `next_stop_id`, `last_communication_at` 을 직접 보관합니다. 버스 목록/상세 조회 시 GPS 이력 테이블을 전혀 건드리지 않아도 최신 상태를 O(1)로 읽을 수 있습니다. GPS 이력(`gps_locations`)은 경로 조회 전용으로만 사용합니다.

---

## 시뮬레이션 구조

```
BusSimulationScheduler (7초 tick)
  ├─ ActiveDispatchCache       ← 운행 중 배차 정보 인메모리 캐시 (DB 조회 없음)
  ├─ RouteStopCache            ← 노선·정류장 정보 3단계 HashMap 캐시
  └─ ExecutorService.invokeAll() — 버스별 병렬 실행 (고정 스레드풀)
       └─ BusTickProcessor     ← 버스별 REQUIRES_NEW 트랜잭션 (단일 실패가 전체 tick에 전파되지 않음)
            ├─ GPS INSERT
            ├─ Bus.updateLocation() (Dirty Checking)
            └─ 이벤트 감지 (가속도 임계값 기반 룰베이스)
```

- **이벤트 감지**: 속도 변화량(m/s²)이 임계값을 초과하면 급정거·급가속·급감속·급출발로 분류, 무작위 확률로 충격 이벤트 발생
- **OFFLINE 시뮬레이션**: 특정 버스 번호는 시뮬레이터에서 의도적으로 제외해 5분 경과 후 OFFLINE 전환이 실제로 동작함을 확인할 수 있습니다

---

## 프로젝트 구조

```
.
├── backend/          Spring Boot 3.x 백엔드
│   └── src/main/java/com/bus/monitoringsystem/api/
│       ├── bus/          버스 목록·상세 API
│       ├── event/        이벤트 목록 API
│       ├── simulator/    7초 tick 시뮬레이터 + 캐시
│       ├── retention/    GPS 24h TTL 스케줄러
│       └── seed/         Mock 데이터 초기화
├── frontend/         Vue 3 프론트엔드
│   └── src/
│       ├── views/        DashboardView, BusDetailView
│       ├── components/   BusMap, BusList, BusEventList
│       └── stores/       bus, event (Pinia)
└── docs/             설계 문서
    └── 설계.md       ERD · 시스템 구조 · 인덱스 전략 · 확장 방안
```

---

상세 설계 결정 사항은 [docs/설계.md](docs/설계.md)를 참조하세요.

---

## 설계 질문 답변

[설계-질문-답변.md](설계-질문-답변.md)

- 질문 1: 서울시 버스 7,000대 동시접속 시 확장 방안
- 질문 2: 버스 전면·후면 블랙박스 및 내부 CCTV 영상 추가 시 설계 방안
