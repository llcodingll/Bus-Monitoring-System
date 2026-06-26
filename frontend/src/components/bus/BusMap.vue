<script setup lang="ts">
import { computed } from 'vue'
import type { BusSummary } from '@/types/bus'

const props = defineProps<{
  buses: BusSummary[]
  selectedBusId: number | null
}>()

const emit = defineEmits<{
  select: [id: number]
  deselect: []
}>()

const selectedBus = computed(() =>
  props.buses.find(b => b.id === props.selectedBusId) ?? null,
)

function markerLeft(lng: number): string {
  return `${((lng - 126.8) / (127.2 - 126.8)) * 100}%`
}

function markerTop(lat: number): string {
  return `${((37.72 - lat) / (37.72 - 37.44)) * 100}%`
}
</script>

<template>
  <section class="relative flex min-h-0 flex-1 overflow-hidden rounded-card bg-card shadow-card">

    <div class="map-grid absolute inset-0 opacity-60"></div>

    <!-- Bus markers -->
    <div class="absolute inset-0">
      <div
        v-for="bus in buses"
        :key="bus.id"
        class="absolute cursor-pointer"
        :style="{ left: markerLeft(bus.currentLongitude), top: markerTop(bus.currentLatitude), transform: 'translate(-50%, -50%)' }"
        @click="emit('select', bus.id)"
      >
        <div
          class="flex items-center justify-center rounded-full text-[10px] font-bold text-white shadow-md transition-transform hover:scale-110"
          :class="[
            bus.status === 'ONLINE' ? 'bg-apple-blue' : 'bg-label-tertiary',
            bus.id === selectedBusId ? 'h-9 w-9 ring-2 ring-white ring-offset-1' : 'h-7 w-7',
          ]"
        >{{ bus.routeNumber }}</div>
        <div
          v-if="bus.id === selectedBusId"
          class="absolute -bottom-1 left-1/2 h-2 w-2 -translate-x-1/2 translate-y-full rotate-45 bg-card"
        ></div>
      </div>
    </div>

    <!-- Selected bus detail overlay -->
    <Transition name="slide-up">
      <div
        v-if="selectedBus"
        class="absolute bottom-4 left-1/2 z-10 w-80 -translate-x-1/2 rounded-card bg-card/90 p-4 shadow-[0_8px_32px_rgba(0,0,0,0.16)] backdrop-blur"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-[15px] font-bold text-label">{{ selectedBus.busNumber }}</p>
            <p class="text-xs text-label-secondary">{{ selectedBus.routeNumber }}번 · {{ selectedBus.routeName }}</p>
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
            <p class="truncate text-sm font-semibold text-label">{{ selectedBus.nextStopName ?? '-' }}</p>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Hint when nothing is selected -->
    <div v-if="!selectedBus" class="pointer-events-none absolute inset-0 flex items-end justify-center pb-6">
      <p class="rounded-full bg-card/70 px-4 py-1.5 text-xs text-label-secondary backdrop-blur">
        버스를 선택하면 상세 정보를 확인할 수 있습니다
      </p>
    </div>

    <div class="absolute right-3 top-3 rounded-button bg-card/80 px-3 py-1.5 backdrop-blur">
      <p class="text-[11px] font-semibold text-label-secondary">서울특별시</p>
    </div>
  </section>
</template>

<style scoped>
.map-grid {
  background-color: #EFF6FF;
  background-image:
    linear-gradient(rgba(59, 130, 246, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.08) 1px, transparent 1px),
    linear-gradient(rgba(59, 130, 246, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.04) 1px, transparent 1px);
  background-size: 80px 80px, 80px 80px, 20px 20px, 20px 20px;
}

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
