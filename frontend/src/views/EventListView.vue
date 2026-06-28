<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import SeverityBadge from '@/components/common/SeverityBadge.vue'
import EventTypeLabel from '@/components/common/EventTypeLabel.vue'
import { useEventStore } from '@/stores/event'
import type { EventType, Severity } from '@/types/bus'

const store = useEventStore()

const selectedEventType = ref<EventType | ''>('')
const selectedSeverity = ref<Severity | ''>('')

onMounted(() => store.loadEvents(0))

function applyFilters(): void {
  store.applyFilters(
    selectedEventType.value || null,
    selectedSeverity.value || null,
  )
}

function resetFilters(): void {
  selectedEventType.value = ''
  selectedSeverity.value = ''
  store.applyFilters(null, null)
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString('ko-KR', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}
</script>

<template>
  <div class="flex h-screen flex-col overflow-hidden bg-surface font-apple">
    <AppHeader />

    <main class="flex-1 overflow-y-auto">
      <div class="mx-auto max-w-screen-2xl p-5">

        <div class="mb-4 flex items-center justify-between">
          <div>
            <h2 class="text-[17px] font-bold text-label">이벤트 목록</h2>
            <p class="mt-0.5 text-xs text-label-secondary">총 {{ store.totalElements }}건</p>
          </div>
          <div class="flex items-center gap-2">
            <select
              v-model="selectedEventType"
              class="rounded-button border border-separator bg-card px-3 py-1.5 text-sm text-label focus:outline-none"
              @change="applyFilters"
            >
              <option value="">유형 전체</option>
              <option value="SUDDEN_BRAKE">급정거</option>
              <option value="SUDDEN_ACCELERATION">급가속</option>
              <option value="SUDDEN_START">급출발</option>
              <option value="SUDDEN_DECELERATION">급감속</option>
              <option value="IMPACT">충격</option>
            </select>
            <select
              v-model="selectedSeverity"
              class="rounded-button border border-separator bg-card px-3 py-1.5 text-sm text-label focus:outline-none"
              @change="applyFilters"
            >
              <option value="">심각도 전체</option>
              <option value="HIGH">높음</option>
              <option value="MEDIUM">보통</option>
              <option value="LOW">낮음</option>
            </select>
            <button
              v-if="selectedEventType || selectedSeverity"
              class="rounded-button px-3 py-1.5 text-sm text-label-secondary hover:text-label"
              @click="resetFilters"
            >
              초기화
            </button>
          </div>
        </div>

        <!-- Loading -->
        <div v-if="store.loading" class="flex items-center justify-center py-20">
          <div class="h-8 w-8 animate-spin rounded-full border-2 border-apple-blue border-t-transparent"></div>
        </div>

        <!-- Empty -->
        <div v-else-if="store.isEmpty" class="flex flex-col items-center justify-center py-20 text-center">
          <p class="text-sm font-medium text-label">기록된 이벤트가 없습니다</p>
          <p class="mt-1 text-xs text-label-secondary">버스 시뮬레이션이 실행되면 이벤트가 표시됩니다</p>
        </div>

        <!-- Error -->
        <div v-else-if="store.error" class="rounded-card bg-red-50 p-4 text-sm text-apple-red">
          {{ store.error }}
        </div>

        <!-- Table -->
        <div v-else class="overflow-hidden rounded-card bg-card shadow-card">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-separator text-left text-xs text-label-secondary">
                <th class="px-4 py-3 font-medium">유형</th>
                <th class="px-4 py-3 font-medium">심각도</th>
                <th class="px-4 py-3 font-medium">버스번호</th>
                <th class="px-4 py-3 font-medium">노선</th>
                <th class="px-4 py-3 font-medium">발생시간</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="event in store.events"
                :key="event.id"
                class="border-b border-separator last:border-0 hover:bg-surface"
              >
                <td class="px-4 py-3">
                  <EventTypeLabel :type="event.eventType" />
                </td>
                <td class="px-4 py-3">
                  <SeverityBadge :severity="event.severity" />
                </td>
                <td class="px-4 py-3 font-medium text-label">{{ event.busNumber }}</td>
                <td class="px-4 py-3 text-label-secondary">{{ event.routeName }}</td>
                <td class="px-4 py-3 tabular-nums text-label-secondary">{{ formatDate(event.occurredAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div v-if="store.totalPages > 1" class="mt-4 flex items-center justify-center gap-2">
          <button
            class="rounded-button px-4 py-2 text-sm font-medium text-label disabled:opacity-30"
            :disabled="!store.hasPrev"
            @click="store.loadEvents(store.currentPage - 1)"
          >
            이전
          </button>
          <span class="text-xs text-label-secondary">
            {{ store.currentPage + 1 }} / {{ store.totalPages }}
          </span>
          <button
            class="rounded-button px-4 py-2 text-sm font-medium text-label disabled:opacity-30"
            :disabled="!store.hasNext"
            @click="store.loadEvents(store.currentPage + 1)"
          >
            다음
          </button>
        </div>

      </div>
    </main>
  </div>
</template>
