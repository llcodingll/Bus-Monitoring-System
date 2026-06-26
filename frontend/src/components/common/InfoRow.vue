<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
}>()
</script>

<template>
  <div class="flex items-center justify-between py-2.5">
    <span class="text-sm text-label-secondary">{{ label }}</span>
    <span class="text-sm font-medium text-label">{{ value }}</span>
  </div>
</template>
