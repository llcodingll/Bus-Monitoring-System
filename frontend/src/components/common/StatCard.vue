<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
  sub?: string
  accent?: boolean
}>()
</script>

<template>
  <div class="rounded-card bg-card p-5 shadow-card">
    <p class="text-xs font-semibold uppercase tracking-wide text-label-secondary">{{ label }}</p>
    <p class="mt-1.5 text-3xl font-bold" :class="accent ? 'text-apple-blue' : 'text-label'">
      {{ value }}
    </p>
    <p v-if="sub" class="mt-1 text-xs text-label-secondary">{{ sub }}</p>
  </div>
</template>
