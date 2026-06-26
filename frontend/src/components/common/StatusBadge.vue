<script setup lang="ts">
import type { BusStatus } from '@/types/bus'

const props = defineProps<{ status: BusStatus }>()

const CONFIG: Record<BusStatus, { wrap: string; dot: string; label: string }> = {
  ONLINE:  { wrap: 'bg-green-50 text-apple-green',  dot: 'bg-apple-green',  label: '온라인' },
  OFFLINE: { wrap: 'bg-red-50 text-apple-red',      dot: 'bg-apple-red',    label: '오프라인' },
}
</script>

<template>
  <span
    class="inline-flex items-center gap-1.5 rounded-badge px-2.5 py-1 text-xs font-semibold"
    :class="CONFIG[props.status].wrap"
  >
    <span class="size-1.5 rounded-full" :class="CONFIG[props.status].dot" />
    {{ CONFIG[props.status].label }}
  </span>
</template>
