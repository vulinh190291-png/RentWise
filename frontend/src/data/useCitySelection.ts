import { computed, ref, watch, type Ref } from 'vue'
import { matchesCity, type ExploreCity } from './explore'

// The future map must consume selectedId and call selectCity, not keep its own selection.
export function useCitySelection(cities: Ref<ExploreCity[]>) {
  const query = ref('')
  const selectedId = ref('')
  const filtered = computed(() => cities.value.filter(city => matchesCity(city, query.value)))
  watch(filtered, list => {
    if (!list.some(city => city.id === selectedId.value)) selectedId.value = list[0]?.id ?? ''
  }, { immediate: true, flush: 'sync' })
  const selected = computed(() => cities.value.find(city => city.id === selectedId.value))
  function selectCity(id: string): boolean {
    if (!cities.value.some(city => city.id === id)) return false
    // A map may select a city outside the current search result.
    if (!filtered.value.some(city => city.id === id)) query.value = ''
    selectedId.value = id
    return true
  }
  return { query, selectedId, filtered, selected, selectCity }
}
