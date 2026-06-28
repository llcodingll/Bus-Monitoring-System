import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { fetchEvents } from '@/api/events'
import type { EventSummary, EventType, Severity } from '@/types/bus'

const PAGE_SIZE = 20

export const useEventStore = defineStore('event', () => {

  const events = ref<EventSummary[]>([])
  const totalElements = ref(0)
  const totalPages = ref(0)
  const currentPage = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const filterEventType = ref<EventType | null>(null)
  const filterSeverity = ref<Severity | null>(null)

  const isEmpty = computed(() => !loading.value && events.value.length === 0)
  const hasNext = computed(() => currentPage.value < totalPages.value - 1)
  const hasPrev = computed(() => currentPage.value > 0)

  async function loadEvents(page = 0): Promise<void> {

    loading.value = true
    error.value = null
    try {
      const result = await fetchEvents(page, PAGE_SIZE, filterEventType.value, filterSeverity.value)
      events.value = result.content
      totalElements.value = result.totalElements
      totalPages.value = result.totalPages
      currentPage.value = result.currentPage
    } catch (e) {
      error.value = e instanceof Error ? e.message : '이벤트 목록을 불러오지 못했습니다.'
    } finally {
      loading.value = false
    }
  }

  function applyFilters(eventType: EventType | null, severity: Severity | null): void {
    filterEventType.value = eventType
    filterSeverity.value = severity
    loadEvents(0)
  }

  return {
    events,
    totalElements,
    totalPages,
    currentPage,
    loading,
    error,
    isEmpty,
    hasNext,
    hasPrev,
    filterEventType,
    filterSeverity,
    loadEvents,
    applyFilters,
  }
})
