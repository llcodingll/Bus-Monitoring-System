export type BusStatus = 'ONLINE' | 'OFFLINE'

export type Direction = 'OUTBOUND' | 'INBOUND'

export type EventType =
  | 'SUDDEN_BRAKE'
  | 'SUDDEN_ACCELERATION'
  | 'SUDDEN_START'
  | 'SUDDEN_DECELERATION'
  | 'IMPACT'

export type Severity = 'LOW' | 'MEDIUM' | 'HIGH'

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
