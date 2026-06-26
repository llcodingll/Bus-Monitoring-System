<script setup lang="ts">
import { ref, computed } from 'vue'
import BusCard from '@/components/bus/BusCard.vue'
import type { BusSummary } from '@/types/bus'

type FilterStatus = 'ALL' | 'ONLINE' | 'OFFLINE'

const props = defineProps<{
  buses: BusSummary[]
  selectedBusId: number | null
}>()

const emit = defineEmits<{
  select: [id: number]
}>()

const filterStatus = ref<FilterStatus>('ALL')

const filteredBuses = computed(() => {
  if (filterStatus.value === 'ALL') return props.buses
  return props.buses.filter(b => b.status === filterStatus.value)
})

const FILTER_OPTIONS: { value: FilterStatus; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'ONLINE', label: '온라인' },
  { value: 'OFFLINE', label: '오프라인' },
]
</script>

<template>
  <aside class="flex w-80 shrink-0 flex-col gap-3">
    <div class="flex shrink-0 gap-1 rounded-card bg-card p-1 shadow-card">
      <button
        v-for="opt in FILTER_OPTIONS"
        :key="opt.value"
        class="flex-1 rounded-[10px] py-1.5 text-xs font-semibold transition-colors duration-100"
        :class="filterStatus === opt.value
          ? 'bg-apple-blue text-white shadow-sm'
          : 'text-label-secondary hover:text-label'"
        @click="filterStatus = opt.value"
      >
        {{ opt.label }}
      </button>
    </div>

    <div class="flex-1 space-y-2 overflow-y-auto pb-2 pr-0.5">
      <BusCard
        v-for="bus in filteredBuses"
        :key="bus.id"
        :bus="bus"
        :selected="bus.id === selectedBusId"
        @select="emit('select', $event)"
      />
      <p
        v-if="filteredBuses.length === 0"
        class="py-8 text-center text-sm text-label-secondary"
      >
        해당 조건의 버스가 없습니다.
      </p>
    </div>
  </aside>
</template>
