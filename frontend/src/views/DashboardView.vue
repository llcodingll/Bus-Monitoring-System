<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import StatCard from '@/components/common/StatCard.vue'
import BusListPanel from '@/components/bus/BusListPanel.vue'
import BusMap from '@/components/bus/BusMap.vue'
import { useBusStore } from '@/stores/bus'
import { usePolling } from '@/composables/usePolling'

const POLLING_INTERVAL_MS = 7_000

const store = useBusStore()
const selectedBusId = ref<number | null>(null)

const { start: startPolling } = usePolling(store.loadBuses, POLLING_INTERVAL_MS)

onMounted(async () => {
  await store.loadBuses()
  startPolling()
})
</script>

<template>
  <div class="flex h-screen flex-col overflow-hidden bg-surface font-apple">
    <AppHeader />

    <main class="min-h-0 flex-1 overflow-hidden">
      <div class="mx-auto flex h-full max-w-screen-2xl flex-col gap-4 p-5">

        <!-- Stats -->
        <div class="grid shrink-0 grid-cols-4 gap-3">
          <StatCard label="전체 버스" :value="store.buses.length" sub="등록된 차량 수" />
          <StatCard label="온라인" :value="store.onlineCount" sub="현재 운행 중" :accent="true" />
          <StatCard label="오프라인" :value="store.offlineCount"
            :sub="store.offlineCount > 0 ? '통신 이상' : '이상 없음'" />
          <StatCard label="금일 이벤트" value="5" sub="위험 운행 감지" />
        </div>

        <!-- Loading (초기 로딩 시에만 표시 — 폴링 갱신 시 BusMap 언마운트 방지) -->
        <div v-if="store.loading && store.buses.length === 0" class="flex flex-1 items-center justify-center">
          <div class="text-center">
            <div class="mx-auto h-8 w-8 animate-spin rounded-full border-2 border-apple-blue border-t-transparent"></div>
            <p class="mt-3 text-sm text-label-secondary">데이터를 불러오는 중...</p>
          </div>
        </div>

        <!-- Empty state -->
        <div
          v-else-if="store.buses.length === 0"
          class="flex flex-1 items-center justify-center"
        >
          <div class="text-center">
            <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-surface">
              <svg class="h-8 w-8 text-label-tertiary" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.129-.504 1.09-1.124a17.902 17.902 0 00-3.213-9.193 2.056 2.056 0 00-1.58-.86H14.25M16.5 18.75h-2.25m0-11.177v-.958c0-.568-.422-1.048-.987-1.106a48.554 48.554 0 00-10.026 0 1.106 1.106 0 00-.987 1.106v7.635m12-6.677v6.677m0 4.5v-4.5m0 0h-12" />
              </svg>
            </div>
            <h2 class="mt-4 text-[17px] font-bold text-label">등록된 버스 데이터가 없습니다</h2>
            <p class="mt-1.5 text-sm text-label-secondary">목업 데이터를 생성하여 시스템을 시작하세요</p>
            <p v-if="store.error" class="mt-2 text-xs text-apple-red">{{ store.error }}</p>
            <button
              class="mt-6 rounded-button bg-apple-blue px-6 py-2.5 text-sm font-semibold text-white shadow-sm transition-opacity hover:opacity-90 disabled:opacity-50"
              :disabled="store.seeding"
              @click="store.seedData()"
            >
              <span v-if="store.seeding" class="flex items-center gap-2">
                <span class="h-3.5 w-3.5 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
                생성 중...
              </span>
              <span v-else>목업 데이터 생성</span>
            </button>
          </div>
        </div>

        <!-- Split view -->
        <div v-else class="flex min-h-0 flex-1 gap-4">
          <BusListPanel
            :buses="store.buses"
            :selected-bus-id="selectedBusId"
            @select="selectedBusId = $event"
          />
          <BusMap
            :buses="store.buses"
            :selected-bus-id="selectedBusId"
            @select="selectedBusId = $event"
            @deselect="selectedBusId = null"
          />
        </div>

      </div>
    </main>
  </div>
</template>
