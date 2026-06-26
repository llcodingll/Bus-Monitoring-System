import axios from 'axios'
import type { BaseResponse } from '@/types/api'

const client = axios.create({ baseURL: '' })

export async function apiGet<T>(path: string, params?: object): Promise<T> {

  const { data } = await client.get<BaseResponse<T>>(path, { params })
  if (!data.isSuccess) throw new Error(data.message)
  return data.result
}

export async function apiPost<T>(path: string, body?: object): Promise<T> {

  const { data } = await client.post<BaseResponse<T>>(path, body)
  if (!data.isSuccess) throw new Error(data.message)
  return data.result
}
