export interface CitySource { title: string; url: string; checkedAt: string }
export interface ExploreCity {
  id: string; name: string; pinyin: string; tag: string
  geography: string; climate: string; culture: string
  housingTitle: string; housing: string; risk: string
  updatedAt: string; sources: CitySource[]
}
const isObject = (v: unknown): v is Record<string, unknown> => typeof v === 'object' && v !== null
const nonempty = (v: unknown): v is string => typeof v === 'string' && v.trim().length > 0
const fields = ['id','name','pinyin','tag','geography','climate','culture','housingTitle','housing','risk','updatedAt'] as const
function isCity(value: unknown): value is ExploreCity {
  if (!isObject(value) || !fields.every(key => nonempty(value[key]))) return false
  return /^[a-z-]+$/.test(String(value.id)) && Array.isArray(value.sources) && value.sources.length > 0 && value.sources.every(source =>
    isObject(source) && nonempty(source.title) && nonempty(source.checkedAt) && nonempty(source.url) && /^https:\/\//.test(source.url))
}
export async function loadCities(signal: AbortSignal): Promise<ExploreCity[]> {
  const response = await fetch(`${import.meta.env.BASE_URL}data/explore-cities.json`, { signal })
  if (!response.ok) throw new Error('城市资料请求失败')
  const data: unknown = await response.json()
  if (!isObject(data) || data.version !== '0.1' || !Array.isArray(data.cities) || !data.cities.every(isCity)) throw new Error('城市资料格式无效')
  const cities: ExploreCity[] = data.cities
  if (new Set(cities.map(city => city.id)).size !== cities.length) throw new Error('城市资料标识重复')
  return cities
}
export function matchesCity(city: ExploreCity, query: string): boolean {
  const key = query.trim().toLocaleLowerCase()
  return [city.name, city.pinyin, city.tag, city.housingTitle].some(text => text.toLocaleLowerCase().includes(key))
}
