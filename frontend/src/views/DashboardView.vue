<script setup lang="ts">
import { ref, computed } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import StatCard from '@/components/common/StatCard.vue'
import BusListPanel from '@/components/bus/BusListPanel.vue'
import BusMap from '@/components/bus/BusMap.vue'
import { MOCK_BUSES } from '@/mocks/buses'

const buses = ref(MOCK_BUSES)
const selectedBusId = ref<number | null>(null)

const onlineCount = computed(() => buses.value.filter(b => b.status === 'ONLINE').length)
const offlineCount = computed(() => buses.value.filter(b => b.status === 'OFFLINE').length)
</script>

<template>
  <div class="flex h-screen flex-col overflow-hidden bg-surface font-apple">
    <AppHeader />

    <main class="min-h-0 flex-1 overflow-hidden">
      <div class="mx-auto flex h-full max-w-screen-2xl flex-col gap-4 p-5">

        <div class="grid shrink-0 grid-cols-4 gap-3">
          <StatCard label="전체 버스" :value="buses.length" sub="등록된 차량 수" />
          <StatCard label="온라인" :value="onlineCount" sub="현재 운행 중" :accent="true" />
          <StatCard label="오프라인" :value="offlineCount" :sub="offlineCount > 0 ? '통신 이상' : '이상 없음'" />
          <StatCard label="금일 이벤트" value="5" sub="위험 운행 감지" />
        </div>

        <div class="flex min-h-0 flex-1 gap-4">
          <BusListPanel
            :buses="buses"
            :selected-bus-id="selectedBusId"
            @select="selectedBusId = $event"
          />
          <BusMap
            :buses="buses"
            :selected-bus-id="selectedBusId"
            @select="selectedBusId = $event"
            @deselect="selectedBusId = null"
          />
        </div>

      </div>
    </main>
  </div>
</template>
