# GPS·이벤트 조회 최적화

> 작업일: 2026-06-26
> 대상: `GET /api/buses/{id}/path`, `GET /api/events`

## 해결한 문제

### 1. gps_locations 복합 인덱스 및 결과 제한

GPS 경로 조회(`GET /api/buses/{id}/path`)는 특정 버스의 최신 위치 이력을 내림차순으로 가져온다.
`bus_id` 단순 인덱스만 있으면 `ORDER BY recorded_at DESC`를 위해 정렬이 별도로 발생한다.

**인덱스:**
```java
@Table(name = "gps_locations",
       indexes = @Index(name = "idx_gps_bus_recorded",
                        columnList = "bus_id, recorded_at DESC"))
```

`(bus_id, recorded_at DESC)` 복합 인덱스로 조건 필터링과 정렬을 인덱스 스캔 한 번으로 처리한다.

**결과 제한:**
```java
@Query("SELECT g FROM GpsLocation g WHERE g.bus.id = :busId ORDER BY g.recordedAt DESC LIMIT 50")
List<GpsLocation> findTop50ByBusIdOrderByRecordedAtDesc(Long busId);
```

7초 주기로 GPS가 쌓이므로 50건은 약 5분 30초 분량이다.
지도에 경로를 표시하기에 충분하고, 전체 이력을 반환하면 응답 크기가 무제한으로 커진다.

---

### 2. events 복합 인덱스

이벤트 목록(`GET /api/events`)은 전체 또는 버스별로 최신순 조회한다.

```java
@Table(name = "events",
       indexes = @Index(name = "idx_events_bus_occurred",
                        columnList = "bus_id, occurred_at DESC"))
```

버스별 필터(`WHERE bus_id = ?`) + 최신순 정렬(`ORDER BY occurred_at DESC`)을 복합 인덱스 하나로 커버.
전체 조회 시에도 `occurred_at DESC` 단일 컬럼으로 인덱스를 활용한다.

---

### 3. 이벤트 조회 N+1 방지 JOIN FETCH

이벤트 목록에는 버스 번호와 노선 번호가 함께 표시된다.
`Event`가 `Bus`, `Route`를 LAZY로 참조하므로 별도 FETCH 없이 접근하면 N+1이 발생한다.

**수정 전:**
```java
// 이벤트 N건 → Bus N번 SELECT + Route N번 SELECT
eventRepository.findAll(pageable);
```

**수정 후:**
```java
@Query("SELECT e FROM Event e JOIN FETCH e.bus JOIN FETCH e.route ORDER BY e.occurredAt DESC")
Page<Event> findAllWithBusAndRoute(Pageable pageable);

@Query("SELECT e FROM Event e JOIN FETCH e.bus JOIN FETCH e.route WHERE e.bus.id = :busId ORDER BY e.occurredAt DESC")
Page<Event> findAllByBusIdWithBusAndRoute(Long busId, Pageable pageable);
```

이벤트와 Bus, Route를 단일 쿼리로 함께 조회한다.

---

### 4. 버스 목록 N+1 방지 JOIN FETCH

`GET /api/buses` 는 버스마다 현재 정류장·다음 정류장 이름이 필요하다.
`Bus.currentStop`, `Bus.nextStop`이 LAZY이므로 반드시 FETCH가 필요하다.

```java
// BusRepository
@Query("SELECT b FROM Bus b LEFT JOIN FETCH b.currentStop LEFT JOIN FETCH b.nextStop")
List<Bus> findAllWithStops();
```

정류장이 없는 버스(미배정)도 포함해야 하므로 `LEFT JOIN FETCH` 사용.
버스 목록 전체를 쿼리 1번으로 조회한다.

## 커밋 대상 파일

```
backend/src/main/java/.../api/gps/model/GpsLocation.java
backend/src/main/java/.../api/gps/repository/GpsLocationRepository.java
backend/src/main/java/.../api/event/model/Event.java
backend/src/main/java/.../api/event/repository/EventRepository.java
backend/src/main/java/.../api/bus/repository/BusRepository.java
```
