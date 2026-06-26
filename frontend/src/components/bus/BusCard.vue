<script setup lang="ts">
import { useRouter } from 'vue-router'
import StatusBadge from '@/components/common/StatusBadge.vue'
import type { BusSummary } from '@/types/bus'

const props = defineProps<{
  bus: BusSummary
  selected?: boolean
}>()

const emit = defineEmits<{
  select: [id: number]
}>()

const router = useRouter()

function navigateToDetail(): void {
  router.push(`/buses/${props.bus.id}`)
}

function formatLastSeen(isoString: string): string {
  const diff = Math.floor((Date.now() - new Date(isoString).getTime()) / 1000)
  if (diff < 60) return '방금 전'
  if (diff < 3600) return `${Math.floor(diff / 60)}분 전`
  return `${Math.floor(diff / 3600)}시간 전`
}
</script>

<template>
  <div
    class="rounded-card bg-card p-4 shadow-card cursor-pointer transition-all duration-150 select-none"
    :class="selected ? 'ring-2 ring-apple-blue shadow-md' : 'hover:shadow-md hover:-translate-y-px'"
    @click="navigateToDetail"
  >
    <div class="flex items-start justify-between gap-2">
      <div class="min-w-0">
        <p class="truncate text-[15px] font-bold text-label">{{ bus.busNumber }}</p>
        <p class="mt-0.5 truncate text-xs text-label-secondary">
          {{ bus.routeNumber }}번 · {{ bus.routeName }}
        </p>
      </div>
      <StatusBadge :status="bus.status" class="shrink-0 mt-0.5" />
    </div>

    <div class="mt-3 flex items-center gap-1 text-xs text-label-secondary">
      <span
        class="inline-block max-w-[7.5rem] truncate rounded-badge bg-surface px-1.5 py-0.5 font-medium text-label"
      >{{ bus.currentStopName ?? '-' }}</span>
      <span class="shrink-0 text-label-tertiary">→</span>
      <span class="truncate">{{ bus.nextStopName ?? '-' }}</span>
    </div>

    <div class="mt-3 flex items-center justify-between border-t border-separator pt-3">
      <div class="flex items-baseline gap-0.5">
        <span
          class="text-xl font-bold"
          :class="bus.status === 'ONLINE' ? 'text-label' : 'text-label-tertiary'"
        >{{ bus.currentSpeed }}</span>
        <span class="text-xs text-label-secondary">km/h</span>
      </div>
      <span class="text-xs text-label-secondary">{{ formatLastSeen(bus.lastCommunicationAt) }}</span>
    </div>
  </div>
</template>
