<script setup lang="ts">
import { useCurrentTime } from '@/composables/useCurrentTime'
import { RouterLink } from 'vue-router'

const { currentTime } = useCurrentTime()
</script>

<template>
  <header class="z-10 shrink-0 border-b border-separator bg-card/80 px-6 py-3 backdrop-blur">
    <div class="mx-auto flex max-w-screen-2xl items-center justify-between">
      <div class="flex items-center gap-3">
        <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-apple-blue">
          <svg class="h-4 w-4 text-white" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.129-.504 1.09-1.124a17.902 17.902 0 00-3.213-9.193 2.056 2.056 0 00-1.58-.86H14.25M16.5 18.75h-2.25m0-11.177v-.958c0-.568-.422-1.048-.987-1.106a48.554 48.554 0 00-10.026 0 1.106 1.106 0 00-.987 1.106v7.635m12-6.677v6.677m0 4.5v-4.5m0 0h-12" />
          </svg>
        </div>
        <div>
          <h1 class="text-[17px] font-bold leading-tight text-label">서울 버스 관제 시스템</h1>
          <p class="text-[11px] text-label-secondary">Seoul Bus Monitoring</p>
        </div>
      </div>

      <div class="flex items-center gap-4">
        <nav class="flex items-center gap-1">
          <RouterLink
            to="/"
            class="rounded-lg px-3 py-1.5 text-sm font-medium text-label-secondary transition-colors hover:bg-surface hover:text-label"
            active-class="bg-surface text-label"
          >
            대시보드
          </RouterLink>
          <RouterLink
            to="/events"
            class="rounded-lg px-3 py-1.5 text-sm font-medium text-label-secondary transition-colors hover:bg-surface hover:text-label"
            active-class="bg-surface text-label"
          >
            이벤트
          </RouterLink>
        </nav>
        <div class="flex items-center gap-1.5 text-xs text-label-secondary">
          <span class="h-1.5 w-1.5 animate-pulse rounded-full bg-apple-green"></span>
          실시간 업데이트 중
        </div>
        <p class="text-[15px] font-semibold tabular-nums text-label">{{ currentTime }}</p>
      </div>
    </div>
  </header>
</template>
