<script setup lang="ts">
defineProps<{
  title: string
  sub?: string
}>()
</script>

<template>
  <div class="mb-4">
    <h2 class="text-xl font-bold text-label">{{ title }}</h2>
    <p v-if="sub" class="mt-0.5 text-sm text-label-secondary">{{ sub }}</p>
  </div>
</template>
