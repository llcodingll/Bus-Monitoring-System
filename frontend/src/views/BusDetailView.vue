<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import InfoRow from '@/components/common/InfoRow.vue'
import SeverityBadge from '@/components/common/SeverityBadge.vue'
import EventTypeLabel from '@/components/common/EventTypeLabel.vue'
import { useBusStore } from '@/stores/bus'

const route = useRoute()
const router = useRouter()
const store = useBusStore()

const busId = Number(route.params.id)

onMounted(async () => {
  await store.loadBusDetail(busId)
  await store.loadBusEvents(busId, 0)
})

const bus = computed(() => store.currentBus)

function formatDateTime(iso: string | null): string {
  if (!iso) return '-'
  return new Date(iso).toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
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

function formatDirection(dir: string | null): string {
  if (!dir) return '-'
  return dir === 'OUTBOUND' ? '정방향' : '역방향'
}
</script>

<template>
  <div class="flex h-screen flex-col overflow-hidden bg-surface font-apple">
    <AppHeader />

    <main class="flex-1 overflow-y-auto">
      <div class="mx-auto max-w-3xl p-5">

        <!-- Back button -->
        <button
          class="mb-4 flex items-center gap-1.5 text-sm text-label-secondary hover:text-label"
          @click="router.back()"
        >
          <svg class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5" />
          </svg>
          뒤로
        </button>

        <!-- Loading -->
        <div v-if="store.currentBusLoading" class="flex items-center justify-center py-20">
          <div class="h-8 w-8 animate-spin rounded-full border-2 border-apple-blue border-t-transparent"></div>
        </div>

        <!-- Error -->
        <div v-else-if="store.currentBusError" class="rounded-card bg-red-50 p-4 text-sm text-apple-red">
          {{ store.currentBusError }}
        </div>

        <!-- Content -->
        <template v-else-if="bus">

          <!-- Header -->
          <div class="mb-4 flex items-center justify-between">
            <div>
              <h2 class="text-[22px] font-bold text-label">{{ bus.busNumber }}</h2>
              <p class="mt-0.5 text-sm text-label-secondary">
                {{ bus.routeNumber }}번 · {{ bus.routeName ?? '-' }}
              </p>
            </div>
            <StatusBadge :status="bus.status" />
          </div>

          <!-- Bus Info Card -->
          <div class="rounded-card bg-card p-4 shadow-card">
            <h3 class="mb-1 text-xs font-semibold uppercase tracking-wide text-label-tertiary">운행 정보</h3>
            <div class="divide-y divide-separator">
              <InfoRow label="현재 속도" :value="`${bus.currentSpeed} km/h`" />
              <InfoRow label="현재 정류장" :value="bus.currentStopName ?? '-'" />
              <InfoRow label="다음 정류장" :value="bus.nextStopName ?? '-'" />
              <InfoRow label="운행 방향" :value="formatDirection(bus.direction)" />
              <InfoRow label="운행 시작" :value="formatDateTime(bus.operationStartedAt)" />
            </div>

            <h3 class="mb-1 mt-4 text-xs font-semibold uppercase tracking-wide text-label-tertiary">통신 상태</h3>
            <div class="divide-y divide-separator">
              <InfoRow label="마지막 통신시간" :value="formatDateTime(bus.lastCommunicationAt)" />
            </div>
          </div>

          <!-- Events Section -->
          <div class="mt-5">
            <div class="mb-3 flex items-center justify-between">
              <h3 class="text-[15px] font-bold text-label">최근 이벤트</h3>
              <span class="text-xs text-label-secondary">총 {{ store.currentBusEventsTotalElements }}건</span>
            </div>

            <div v-if="store.currentBusEventsLoading" class="flex justify-center py-8">
              <div class="h-6 w-6 animate-spin rounded-full border-2 border-apple-blue border-t-transparent"></div>
            </div>

            <div
              v-else-if="store.currentBusEvents.length === 0"
              class="rounded-card bg-card p-6 text-center shadow-card"
            >
              <p class="text-sm text-label-secondary">기록된 이벤트가 없습니다</p>
            </div>

            <div v-else class="overflow-hidden rounded-card bg-card shadow-card">
              <table class="w-full text-sm">
                <thead>
                  <tr class="border-b border-separator text-left text-xs text-label-secondary">
                    <th class="px-4 py-3 font-medium">유형</th>
                    <th class="px-4 py-3 font-medium">심각도</th>
                    <th class="px-4 py-3 font-medium">발생시간</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="event in store.currentBusEvents"
                    :key="event.id"
                    class="border-b border-separator last:border-0 hover:bg-surface"
                  >
                    <td class="px-4 py-3">
                      <EventTypeLabel :type="event.eventType" />
                    </td>
                    <td class="px-4 py-3">
                      <SeverityBadge :severity="event.severity" />
                    </td>
                    <td class="px-4 py-3 tabular-nums text-label-secondary">{{ formatDate(event.occurredAt) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Pagination -->
            <div v-if="store.currentBusEventsTotalPages > 1" class="mt-4 flex items-center justify-center gap-2">
              <button
                class="rounded-button px-4 py-2 text-sm font-medium text-label disabled:opacity-30"
                :disabled="store.currentBusEventsPage === 0"
                @click="store.loadBusEvents(busId, store.currentBusEventsPage - 1)"
              >
                이전
              </button>
              <span class="text-xs text-label-secondary">
                {{ store.currentBusEventsPage + 1 }} / {{ store.currentBusEventsTotalPages }}
              </span>
              <button
                class="rounded-button px-4 py-2 text-sm font-medium text-label disabled:opacity-30"
                :disabled="store.currentBusEventsPage >= store.currentBusEventsTotalPages - 1"
                @click="store.loadBusEvents(busId, store.currentBusEventsPage + 1)"
              >
                다음
              </button>
            </div>
          </div>

        </template>

      </div>
    </main>
  </div>
</template>
