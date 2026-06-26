import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { fetchBuses, fetchBusDetail, fetchBusEvents } from '@/api/buses'
import { triggerSeed } from '@/api/seed'
import type { BusSummary, BusDetail, EventSummary } from '@/types/bus'

export const useBusStore = defineStore('bus', () => {

  const buses = ref<BusSummary[]>([])
  const loading = ref(false)
  const seeding = ref(false)
  const error = ref<string | null>(null)

  const currentBus = ref<BusDetail | null>(null)
  const currentBusLoading = ref(false)
  const currentBusError = ref<string | null>(null)

  const currentBusEvents = ref<EventSummary[]>([])
  const currentBusEventsPage = ref(0)
  const currentBusEventsTotalPages = ref(0)
  const currentBusEventsTotalElements = ref(0)
  const currentBusEventsLoading = ref(false)

  const onlineCount = computed(() => buses.value.filter(b => b.status === 'ONLINE').length)
  const offlineCount = computed(() => buses.value.filter(b => b.status === 'OFFLINE').length)

  async function loadBuses(): Promise<void> {

    loading.value = true
    error.value = null
    try {
      buses.value = await fetchBuses()
    } catch (e) {
      error.value = e instanceof Error ? e.message : '버스 목록을 불러오지 못했습니다.'
    } finally {
      loading.value = false
    }
  }

  async function loadBusDetail(id: number): Promise<void> {

    currentBusLoading.value = true
    currentBusError.value = null
    try {
      currentBus.value = await fetchBusDetail(id)
    } catch (e) {
      currentBusError.value = e instanceof Error ? e.message : '버스 정보를 불러오지 못했습니다.'
    } finally {
      currentBusLoading.value = false
    }
  }

  async function loadBusEvents(busId: number, page = 0): Promise<void> {

    currentBusEventsLoading.value = true
    try {
      const result = await fetchBusEvents(busId, page)
      currentBusEvents.value = result.content
      currentBusEventsPage.value = result.currentPage
      currentBusEventsTotalPages.value = result.totalPages
      currentBusEventsTotalElements.value = result.totalElements
    } catch {
      currentBusEvents.value = []
    } finally {
      currentBusEventsLoading.value = false
    }
  }

  async function seedData(): Promise<void> {

    seeding.value = true
    error.value = null
    try {
      await triggerSeed()
      await loadBuses()
    } catch (e) {
      error.value = e instanceof Error ? e.message : '데이터 생성에 실패했습니다.'
    } finally {
      seeding.value = false
    }
  }

  return {
    buses, loading, seeding, error, onlineCount, offlineCount,
    currentBus, currentBusLoading, currentBusError,
    currentBusEvents, currentBusEventsPage, currentBusEventsTotalPages,
    currentBusEventsTotalElements, currentBusEventsLoading,
    loadBuses, loadBusDetail, loadBusEvents, seedData,
  }
})
