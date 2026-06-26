<script setup lang="ts">
import type { Severity } from '@/types/bus'

const props = defineProps<{ severity: Severity }>()

const CONFIG: Record<Severity, { wrap: string; label: string }> = {
  HIGH:   { wrap: 'bg-red-50 text-apple-red',            label: '높음' },
  MEDIUM: { wrap: 'bg-orange-50 text-apple-orange',       label: '보통' },
  LOW:    { wrap: 'bg-gray-100 text-label-secondary',     label: '낮음' },
}
</script>

<template>
  <span
    class="inline-flex items-center rounded-badge px-2.5 py-1 text-xs font-semibold"
    :class="CONFIG[props.severity].wrap"
  >
    {{ CONFIG[props.severity].label }}
  </span>
</template>
