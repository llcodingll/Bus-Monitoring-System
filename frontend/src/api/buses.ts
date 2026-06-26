import { apiGet } from '@/api/client'
import type { BusSummary } from '@/types/bus'

export function fetchBuses(): Promise<BusSummary[]> {
  return apiGet<BusSummary[]>('/api/buses')
}
