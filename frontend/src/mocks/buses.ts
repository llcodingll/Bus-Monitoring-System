import type { BusSummary } from '@/types/bus'

export const MOCK_BUSES: BusSummary[] = [
  {
    id: 1, busNumber: '서울75바1234', routeNumber: '370', routeName: '상계동 - 서울역',
    currentSpeed: 42, status: 'ONLINE',
    lastCommunicationAt: new Date(Date.now() - 45_000).toISOString(),
    currentStopName: '노원역', nextStopName: '미아사거리',
    direction: 'OUTBOUND', currentLatitude: 37.6558, currentLongitude: 127.0566,
  },
  {
    id: 2, busNumber: '서울75바2345', routeNumber: '370', routeName: '상계동 - 서울역',
    currentSpeed: 28, status: 'ONLINE',
    lastCommunicationAt: new Date(Date.now() - 30_000).toISOString(),
    currentStopName: '성신여대입구', nextStopName: '한성대입구',
    direction: 'OUTBOUND', currentLatitude: 37.5930, currentLongitude: 127.0175,
  },
  {
    id: 3, busNumber: '서울75바3456', routeNumber: '370', routeName: '상계동 - 서울역',
    currentSpeed: 0, status: 'OFFLINE',
    lastCommunicationAt: new Date(Date.now() - 480_000).toISOString(),
    currentStopName: '혜화역', nextStopName: '한성대입구',
    direction: 'INBOUND', currentLatitude: 37.5820, currentLongitude: 127.0015,
  },
  {
    id: 4, busNumber: '서울74바4567', routeNumber: '471', routeName: '중계동 - 서울역',
    currentSpeed: 35, status: 'ONLINE',
    lastCommunicationAt: new Date(Date.now() - 60_000).toISOString(),
    currentStopName: '광운대역', nextStopName: '석계역',
    direction: 'OUTBOUND', currentLatitude: 37.6241, currentLongitude: 127.0580,
  },
  {
    id: 5, busNumber: '서울74바5678', routeNumber: '471', routeName: '중계동 - 서울역',
    currentSpeed: 22, status: 'ONLINE',
    lastCommunicationAt: new Date(Date.now() - 20_000).toISOString(),
    currentStopName: '을지로3가', nextStopName: '혜화역',
    direction: 'INBOUND', currentLatitude: 37.5661, currentLongitude: 126.9910,
  },
  {
    id: 6, busNumber: '서울73바6789', routeNumber: '261', routeName: '신촌역 - 잠실역',
    currentSpeed: 31, status: 'ONLINE',
    lastCommunicationAt: new Date(Date.now() - 35_000).toISOString(),
    currentStopName: '홍대입구역', nextStopName: '합정역',
    direction: 'OUTBOUND', currentLatitude: 37.5576, currentLongitude: 126.9245,
  },
  {
    id: 7, busNumber: '서울73바7890', routeNumber: '261', routeName: '신촌역 - 잠실역',
    currentSpeed: 0, status: 'OFFLINE',
    lastCommunicationAt: new Date(Date.now() - 720_000).toISOString(),
    currentStopName: '성수역', nextStopName: '건대입구역',
    direction: 'OUTBOUND', currentLatitude: 37.5446, currentLongitude: 127.0561,
  },
  {
    id: 8, busNumber: '서울72바8901', routeNumber: '143', routeName: '도봉산역 - 서울역',
    currentSpeed: 48, status: 'ONLINE',
    lastCommunicationAt: new Date(Date.now() - 50_000).toISOString(),
    currentStopName: '방학역', nextStopName: '창동역',
    direction: 'OUTBOUND', currentLatitude: 37.6594, currentLongitude: 127.0380,
  },
  {
    id: 9, busNumber: '서울72바9012', routeNumber: '143', routeName: '도봉산역 - 서울역',
    currentSpeed: 37, status: 'ONLINE',
    lastCommunicationAt: new Date(Date.now() - 25_000).toISOString(),
    currentStopName: '길음역', nextStopName: '노원역',
    direction: 'INBOUND', currentLatitude: 37.5930, currentLongitude: 127.0255,
  },
  {
    id: 10, busNumber: '서울71나1234', routeNumber: '500', routeName: '시청앞 - 신림역',
    currentSpeed: 25, status: 'ONLINE',
    lastCommunicationAt: new Date(Date.now() - 40_000).toISOString(),
    currentStopName: '사당역', nextStopName: '이수역',
    direction: 'OUTBOUND', currentLatitude: 37.4762, currentLongitude: 126.9814,
  },
]
