<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import type { BusSummary, GpsPoint } from '@/types/bus'
import { fetchBusPath } from '@/api/buses'

const SEOUL_CENTER: [number, number] = [37.5665, 126.9780]
const DEFAULT_ZOOM = 12
const ANIMATION_DURATION_MS = 7_000
const ANIMATION_STEP_MS = 200

const props = defineProps<{
  buses: BusSummary[]
  selectedBusId: number | null
}>()

const emit = defineEmits<{
  select: [id: number]
  deselect: []
}>()

const mapEl = ref<HTMLElement | null>(null)
let map: L.Map | null = null
const markers = new Map<number, L.Marker>()
const animIntervals = new Map<number, ReturnType<typeof setInterval>>()
let pathLine: L.Polyline | null = null
const arrowMarkers: L.Marker[] = []

const selectedBus = computed(() => props.buses.find(b => b.id === props.selectedBusId) ?? null)

function buildIcon(bus: BusSummary, selected: boolean): L.DivIcon {
  const color = bus.status === 'ONLINE' ? '#007AFF' : '#8E8E93'
  const s = selected ? 36 : 28
  const outline = selected ? 'outline:2.5px solid white;outline-offset:2px;' : ''
  return L.divIcon({
    html: `<div style="width:${s}px;height:${s}px;background:${color};border-radius:50%;display:flex;align-items:center;justify-content:center;color:#fff;font-size:10px;font-weight:700;box-shadow:0 2px 8px rgba(0,0,0,.25);${outline}">${bus.routeNumber}</div>`,
    className: '',
    iconSize: [s, s],
    iconAnchor: [s / 2, s / 2],
  })
}

function getOrCreateMarker(bus: BusSummary): L.Marker {
  if (markers.has(bus.id)) return markers.get(bus.id)!
  const marker = L.marker([bus.currentLatitude, bus.currentLongitude], {
    icon: buildIcon(bus, false),
  })
  marker.on('click', () => emit('select', bus.id))
  marker.addTo(map!)
  markers.set(bus.id, marker)
  return marker
}

function smoothMove(busId: number, marker: L.Marker, targetLat: number, targetLng: number): void {
  const existing = animIntervals.get(busId)
  if (existing) clearInterval(existing)

  const start = marker.getLatLng()
  const startTime = Date.now()

  const id = setInterval(() => {
    const progress = Math.min((Date.now() - startTime) / ANIMATION_DURATION_MS, 1)
    marker.setLatLng([
      start.lat + (targetLat - start.lat) * progress,
      start.lng + (targetLng - start.lng) * progress,
    ])
    if (progress >= 1) clearInterval(id)
  }, ANIMATION_STEP_MS)

  animIntervals.set(busId, id)
}

function syncMarkers(buses: BusSummary[]): void {
  if (!map) return
  const activeIds = new Set(buses.map(b => b.id))

  buses.forEach(bus => {
    const existing = markers.get(bus.id)
    if (existing) {
      smoothMove(bus.id, existing, bus.currentLatitude, bus.currentLongitude)
      existing.setIcon(buildIcon(bus, bus.id === props.selectedBusId))
    } else {
      getOrCreateMarker(bus)
    }
  })

  markers.forEach((marker, id) => {
    if (!activeIds.has(id)) {
      marker.remove()
      markers.delete(id)
      const interval = animIntervals.get(id)
      if (interval) {
        clearInterval(interval)
        animIntervals.delete(id)
      }
    }
  })
}

async function showPath(busId: number): Promise<void> {
  clearPath()
  const currentMap = map
  try {
    const points: GpsPoint[] = await fetchBusPath(busId)
    if (!currentMap || points.length < 2) return
    const latlngs = points.map(p => [p.latitude, p.longitude] as [number, number])
    pathLine = L.polyline(latlngs, { color: '#007AFF', weight: 3, opacity: 0.6 }).addTo(currentMap)
    addArrows(latlngs)
  } catch {
    // 경로 조회 실패 시 조용히 무시
  }
}

function clearPath(): void {
  pathLine?.remove()
  pathLine = null
  arrowMarkers.forEach(m => m.remove())
  arrowMarkers.length = 0
}

function addArrows(latlngs: [number, number][]): void {
  if (!map || latlngs.length < 2) return
  // latlngs[0] = 최신, latlngs[N] = 과거 → 진행방향: 큰 인덱스 → 작은 인덱스
  const step = Math.max(2, Math.floor(latlngs.length / 5))
  for (let i = latlngs.length - 1; i >= step; i -= step) {
    const p1 = latlngs[i]
    const p2 = latlngs[i - step]
    if (!p1 || !p2) continue
    const [lat1, lng1] = p1
    const [lat2, lng2] = p2
    const angleDeg = Math.atan2(lng2 - lng1, lat2 - lat1) * 180 / Math.PI
    const icon = L.divIcon({
      html: `<div style="width:0;height:0;border-left:5px solid transparent;border-right:5px solid transparent;border-bottom:10px solid rgba(0,122,255,0.75);transform:rotate(${angleDeg}deg);transform-origin:50% 60%"></div>`,
      className: '',
      iconSize: [10, 10],
      iconAnchor: [5, 5],
    })
    arrowMarkers.push(L.marker([lat1, lng1], { icon, interactive: false }).addTo(map!))
  }
}

function showCallout(bus: BusSummary, marker: L.Marker): void {
  marker.unbindTooltip()
  const dir = bus.direction === 'OUTBOUND' ? '하행 ↓' : bus.direction === 'INBOUND' ? '상행 ↑' : null
  const next = bus.nextStopName ?? '-'
  const dirHtml = dir
    ? `<span class="callout-dir ${bus.direction === 'OUTBOUND' ? 'outbound' : 'inbound'}">${dir}</span> `
    : ''
  marker.bindTooltip(`${dirHtml}<span class="callout-next">📍 ${next}</span>`, {
    permanent: true,
    direction: 'top',
    offset: [0, -4],
    className: 'bus-callout',
  }).openTooltip()
}

watch(() => props.buses, async (buses) => {
  syncMarkers(buses)
  if (props.selectedBusId != null) {
    const bus = buses.find(b => b.id === props.selectedBusId)
    const marker = markers.get(props.selectedBusId)
    if (bus && marker) showCallout(bus, marker)
    await showPath(props.selectedBusId)
  }
}, { deep: true })

watch(() => props.selectedBusId, async (id, prevId) => {
  if (!map) return

  if (prevId != null) {
    const prevBus = props.buses.find(b => b.id === prevId)
    const prevMarker = markers.get(prevId)
    if (prevBus && prevMarker) {
      prevMarker.setIcon(buildIcon(prevBus, false))
      prevMarker.unbindTooltip()
    }
  }

  clearPath()

  if (id != null) {
    const bus = props.buses.find(b => b.id === id)
    const marker = markers.get(id)
    if (bus && marker) {
      marker.setIcon(buildIcon(bus, true))
      showCallout(bus, marker)
    }
    await showPath(id)
  }
})

onMounted(() => {
  if (!mapEl.value) return
  map = L.map(mapEl.value).setView(SEOUL_CENTER, DEFAULT_ZOOM)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19,
  }).addTo(map)
  syncMarkers(props.buses)
  if (props.selectedBusId != null) {
    const bus = props.buses.find(b => b.id === props.selectedBusId)
    const marker = markers.get(props.selectedBusId)
    if (bus && marker) showCallout(bus, marker)
    showPath(props.selectedBusId)
  }
})

onUnmounted(() => {
  animIntervals.forEach(id => clearInterval(id))
  animIntervals.clear()
  clearPath()
  map?.remove()
  map = null
})
</script>

<template>
  <section class="relative flex min-h-0 flex-1 overflow-hidden rounded-card shadow-card">
    <div ref="mapEl" class="h-full w-full"></div>

    <Transition name="slide-up">
      <div
        v-if="selectedBus"
        class="absolute bottom-4 left-1/2 z-[1000] w-80 -translate-x-1/2 rounded-card bg-card/90 p-4 shadow-[0_8px_32px_rgba(0,0,0,0.16)] backdrop-blur"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-[15px] font-bold text-label">{{ selectedBus.busNumber }}</p>
            <p class="mt-0.5 flex items-center gap-1.5 text-xs text-label-secondary">
              {{ selectedBus.routeNumber }}번 · {{ selectedBus.routeName }}
              <span
                v-if="selectedBus.direction"
                class="rounded px-1.5 py-0.5 text-[10px] font-bold"
                :class="selectedBus.direction === 'OUTBOUND' ? 'bg-blue-50 text-apple-blue' : 'bg-orange-50 text-orange-500'"
              >{{ selectedBus.direction === 'OUTBOUND' ? '하행 ↓' : '상행 ↑' }}</span>
            </p>
          </div>
          <button
            class="rounded-full p-1.5 text-label-secondary hover:bg-surface"
            @click="emit('deselect')"
          >
            <svg class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <div class="mt-3 grid grid-cols-3 gap-3 border-t border-separator pt-3 text-center">
          <div>
            <p class="text-[11px] text-label-secondary">현재 속도</p>
            <p class="text-lg font-bold text-label">
              {{ selectedBus.currentSpeed }}<span class="text-[11px] font-normal text-label-secondary"> km/h</span>
            </p>
          </div>
          <div>
            <p class="text-[11px] text-label-secondary">현재 정류장</p>
            <p class="truncate text-sm font-semibold text-label">{{ selectedBus.currentStopName ?? '-' }}</p>
          </div>
          <div>
            <p class="text-[11px] text-label-secondary">다음 정류장</p>
            <div class="flex items-center justify-center gap-0.5">
              <svg class="h-3 w-3 shrink-0 text-apple-blue" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 10.5a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" />
                <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 1 1 15 0Z" />
              </svg>
              <p class="truncate text-sm font-semibold text-label">{{ selectedBus.nextStopName ?? '-' }}</p>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <div v-if="!selectedBus" class="pointer-events-none absolute bottom-6 left-1/2 z-[1000] -translate-x-1/2">
      <p class="rounded-full bg-card/70 px-4 py-1.5 text-xs text-label-secondary backdrop-blur">
        버스를 선택하면 상세 정보를 확인할 수 있습니다
      </p>
    </div>
  </section>
</template>

<style scoped>
.slide-up-enter-active,
.slide-up-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(8px);
}
</style>

<style>
.bus-callout {
  background: white;
  border: none;
  border-radius: 10px;
  padding: 5px 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.18);
  font-family: -apple-system, 'Helvetica Neue', sans-serif;
  font-size: 12px;
  white-space: nowrap;
}
.bus-callout::before {
  border-top-color: white !important;
}
.callout-dir {
  border-radius: 4px;
  padding: 1px 5px;
  font-weight: 700;
  font-size: 10px;
  margin-right: 4px;
}
.callout-dir.outbound { background: #EBF5FF; color: #007AFF; }
.callout-dir.inbound  { background: #FFF3E0; color: #FF9500; }
.callout-next { color: #1c1c1e; font-weight: 500; }
</style>
