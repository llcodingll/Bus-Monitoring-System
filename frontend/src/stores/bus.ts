import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { fetchBuses } from '@/api/buses'
import { triggerSeed } from '@/api/seed'
import type { BusSummary } from '@/types/bus'

export const useBusStore = defineStore('bus', () => {

  const buses = ref<BusSummary[]>([])
  const loading = ref(false)
  const seeding = ref(false)
  const error = ref<string | null>(null)

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

  return { buses, loading, seeding, error, onlineCount, offlineCount, loadBuses, seedData }
})
