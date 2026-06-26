import { apiGet } from '@/api/client'
import type { BusSummary, BusDetail, GpsPoint } from '@/types/bus'
import type { EventSummary } from '@/types/bus'
import type { PageResult } from '@/types/api'

export function fetchBuses(): Promise<BusSummary[]> {
  return apiGet<BusSummary[]>('/api/buses')
}

export function fetchBusDetail(id: number): Promise<BusDetail> {
  return apiGet<BusDetail>(`/api/buses/${id}`)
}

export function fetchBusEvents(busId: number, page = 0, size = 10): Promise<PageResult<EventSummary>> {
  return apiGet<PageResult<EventSummary>>(`/api/buses/${busId}/events`, { page, size })
}

export function fetchBusPath(busId: number): Promise<GpsPoint[]> {
  return apiGet<GpsPoint[]>(`/api/buses/${busId}/path`)
}
