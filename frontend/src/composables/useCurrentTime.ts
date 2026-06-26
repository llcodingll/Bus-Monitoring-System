import { ref, onMounted, onUnmounted } from 'vue'

export function useCurrentTime() {
  const currentTime = ref('')

  function update(): void {
    currentTime.value = new Date().toLocaleTimeString('ko-KR', {
      hour: '2-digit', minute: '2-digit', second: '2-digit',
    })
  }

  let timer: ReturnType<typeof setInterval>

  onMounted(() => {
    update()
    timer = setInterval(update, 1000)
  })

  onUnmounted(() => clearInterval(timer))

  return { currentTime }
}
