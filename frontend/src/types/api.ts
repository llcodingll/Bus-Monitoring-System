export interface BaseResponse<T> {
  isSuccess: boolean
  code: number
  message: string
  result: T
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  currentPage: number
  size: number
}
