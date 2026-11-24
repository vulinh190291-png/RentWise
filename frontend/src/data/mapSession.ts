// Provider-neutral boundary. No provider, credentials or coordinates are bundled.
export type MapStatus = 'unconfigured' | 'loading' | 'ready' | 'error'
export interface CityMapAdapter {
  selectCity(id: string, animate: boolean): void
  destroy(): void // Must remove SDK listeners as well as the map instance.
}
export type MapFactory = (signal: AbortSignal, onSelect: (id: string) => void) => Promise<CityMapAdapter>
export function createMapSession(onStatus: (status: MapStatus) => void, onSelect: (id: string) => void) {
  let generation = 0
  let controller: AbortController | undefined
  let adapter: CityMapAdapter | undefined
  let timer: ReturnType<typeof setTimeout> | undefined
  let selection = ''
  let animate = false
  function dispose() {
    generation++; controller?.abort(); clearTimeout(timer)
    const previous = adapter; adapter = undefined
    previous?.destroy()
  }
  async function start(factory?: MapFactory, timeoutMs = 12000) {
    dispose()
    if (!factory) { onStatus('unconfigured'); return }
    const current = generation
    controller = new AbortController()
    const signal = controller.signal
    onStatus('loading')
    const fail = () => {
      if (current !== generation) return
      dispose(); onStatus('error')
    }
    timer = setTimeout(fail, timeoutMs)
    try {
      const result = await factory(signal, id => { if (current === generation && !signal.aborted) onSelect(id) })
      if (current !== generation) { result.destroy(); return }
      adapter = result; clearTimeout(timer)
      adapter.selectCity(selection, animate)
      onStatus('ready')
    } catch { fail() }
  }
  function selectCity(id: string, reducedMotion = true) {
    selection = id; animate = !reducedMotion
    try { adapter?.selectCity(id, animate) }
    catch { dispose(); onStatus('error') }
  }
  return { start, selectCity, dispose }
}
