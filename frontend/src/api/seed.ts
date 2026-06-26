import { apiPost } from '@/api/client'

export interface SeedResult {
  message: string
  busCount: number
  routeCount: number
  stopCount: number
  eventCount: number
}

export function triggerSeed(): Promise<SeedResult> {
  return apiPost<SeedResult>('/api/seed')
}
