import { onUnmounted } from 'vue'

export function usePolling(fn: () => void | Promise<void>, intervalMs: number) {

  let timer: ReturnType<typeof setInterval> | null = null

  function start(): void {
    if (timer !== null) return
    timer = setInterval(fn, intervalMs)
  }

  function stop(): void {
    if (timer !== null) {
      clearInterval(timer)
      timer = null
    }
  }

  onUnmounted(stop)

  return { start, stop }
}
