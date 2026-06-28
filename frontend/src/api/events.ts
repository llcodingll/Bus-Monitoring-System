import { apiGet } from '@/api/client'
import type { EventSummary, EventType, Severity } from '@/types/bus'
import type { PageResult } from '@/types/api'

export function fetchEvents(
  page = 0,
  size = 20,
  eventType?: EventType | null,
  severity?: Severity | null,
): Promise<PageResult<EventSummary>> {
  const params: Record<string, unknown> = { page, size }
  if (eventType) params.eventType = eventType
  if (severity) params.severity = severity
  return apiGet<PageResult<EventSummary>>('/api/events', params)
}
