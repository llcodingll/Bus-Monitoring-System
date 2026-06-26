import { apiGet } from '@/api/client'
import type { EventSummary } from '@/types/bus'
import type { PageResult } from '@/types/api'

export function fetchEvents(page = 0, size = 20): Promise<PageResult<EventSummary>> {
  return apiGet<PageResult<EventSummary>>('/api/events', { page, size })
}
