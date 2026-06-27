# 폴링 중 BusMap 언마운트 버그 수정

> 작업일: 2026-06-26
> 대상: `DashboardView.vue` — 버스 목록 로딩 조건

## 해결한 문제

### 1. 폴링 갱신마다 지도가 초기화되는 버그

버스 목록을 30초마다 폴링할 때, `store.loading`이 잠깐 `true`로 바뀐다.
이 값으로 로딩 스피너를 조건 렌더링하면 폴링마다 `BusMap`이 언마운트·리마운트되어:

- Leaflet 지도 인스턴스가 파괴되고 재생성됨
- 버스 마커가 전부 사라졌다가 다시 생성됨
- 선택된 버스 정보·경로 레이어가 초기화됨
- 애니메이션 타이머가 전부 취소됨

**수정 전:**
```html
<div v-if="store.loading">   <!-- 폴링마다 BusMap 언마운트 -->
  <LoadingSpinner />
</div>
<BusMap v-else ... />
```

**수정 후:**
```html
<!-- 초기 로딩 시에만 스피너 표시 — 폴링 갱신 시 BusMap 언마운트 방지 -->
<div v-if="store.loading && store.buses.length === 0">
  <LoadingSpinner />
</div>
```

`store.buses.length === 0` 조건을 추가해 **첫 로딩 때만** 스피너를 보여준다.
이미 버스 데이터가 있는 상태에서 폴링이 발생하면 `store.loading`이 `true`여도 `BusMap`을 유지한다.

폴링 중 화면은 지도와 마커를 유지한 채 데이터만 조용히 갱신된다.

## 커밋 대상 파일

```
frontend/src/views/DashboardView.vue
```

## 커밋 메시지

```
feat: 버스 카드 방향 표시 및 폴링 중 지도 언마운트 버그 수정
```
