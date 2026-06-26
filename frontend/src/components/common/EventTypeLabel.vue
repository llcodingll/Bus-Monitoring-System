<script setup lang="ts">
import type { EventType } from '@/types/bus'

const props = defineProps<{ type: EventType }>()

const LABEL: Record<EventType, string> = {
  SUDDEN_BRAKE:        '급정거',
  SUDDEN_ACCELERATION: '급가속',
  SUDDEN_START:        '급출발',
  SUDDEN_DECELERATION: '급감속',
  IMPACT:              '충격',
}
</script>

<template>
  <span class="text-sm font-medium text-label">{{ LABEL[props.type] }}</span>
</template>
