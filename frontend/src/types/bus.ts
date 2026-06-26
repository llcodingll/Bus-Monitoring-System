export type BusStatus = 'ONLINE' | 'OFFLINE'

export type Direction = 'OUTBOUND' | 'INBOUND'

export type EventType =
  | 'SUDDEN_BRAKE'
  | 'SUDDEN_ACCELERATION'
  | 'SUDDEN_START'
  | 'SUDDEN_DECELERATION'
  | 'IMPACT'

export type Severity = 'LOW' | 'MEDIUM' | 'HIGH'

export interface EventSummary {
  id: number
  busId: number
  busNumber: string
  routeNumber: string
  routeName: string
  eventType: EventType
  severity: Severity
  latitude: number
  longitude: number
  occurredAt: string
}

export interface BusSummary {
  id: number
  busNumber: string
  routeNumber: string
  routeName: string
  currentSpeed: number
  status: BusStatus
  lastCommunicationAt: string
  currentStopName: string | null
  nextStopName: string | null
  direction: Direction | null
  currentLatitude: number
  currentLongitude: number
}

export interface GpsPoint {
  latitude: number
  longitude: number
  recordedAt: string
}

export interface BusDetail {
  id: number
  busNumber: string
  routeNumber: string | null
  routeName: string | null
  currentSpeed: number
  status: BusStatus
  lastCommunicationAt: string | null
  currentStopName: string | null
  nextStopName: string | null
  direction: Direction | null
  operationStartedAt: string | null
  currentLatitude: number | null
  currentLongitude: number | null
}
