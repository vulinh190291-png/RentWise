import type { MapFactory, CityMapAdapter } from './mapSession'
// SDK structural boundary; no SDK code or credentials are included in the bundle.
type EventTarget = { on(name: string, handler: (...args: any[]) => void): void; off(name: string, handler: (...args: any[]) => void): void }
type MapInstance = EventTarget & { setBounds(bounds: unknown, immediately?: boolean, padding?: number[]): void; getZoom(): number; setZoom(zoom: number, immediately?: boolean): void; add(markers: Marker[]): void; destroy(): void; setZoomAndCenter(zoom: number, center: number[], immediately?: boolean): void }
type Marker = EventTarget & { setContent(content: HTMLElement): void }
type SDK = {
 Bounds: new (southwest: number[], northeast: number[]) => unknown
 Map: new (container: HTMLElement, options: object) => MapInstance
 Marker: new (options: object) => Marker
 DistrictSearch: new (options: object) => { search(code: string, cb: (status: string, result: any) => void): void }
 plugin(names: string[], callback: () => void): void
}
declare global { interface Window { AMap?: SDK; _AMapSecurityConfig?: { serviceHost: string } } }
const codes: Record<string,string> = {beijing:'110000',shanghai:'310000',guangzhou:'440100',suzhou:'320500',xiamen:'350200',lijiang:'530700',huangshan:'341000',harbin:'230100'}
function abortable<T>(promise: Promise<T>, signal: AbortSignal): Promise<T> {
 return new Promise((resolve,reject)=>{
  const abort=()=>reject(new Error('cancelled'))
  if(signal.aborted){abort();return}
  signal.addEventListener('abort',abort,{once:true})
  promise.then(resolve,reject).finally(()=>signal.removeEventListener('abort',abort))
 })
}
const centers = new Map<string, number[]>()
let sdkPromise: Promise<SDK> | undefined
async function loadSDK(): Promise<SDK> {
 if (window.AMap) return window.AMap
 if (!sdkPromise) sdkPromise = (async()=>{
  const response = await fetch('/map-local/config',{signal:AbortSignal.timeout(8000)})
  if (!response.ok) throw new Error('地图代理尚未就绪')
  const config = await response.json()
  if (typeof config.key !== 'string' || config.serviceHost !== '/_AMapService') throw new Error('地图配置无效')
  window._AMapSecurityConfig = {serviceHost:location.origin+'/_AMapService'}
  return await new Promise<SDK>((resolve,reject)=>{
   const script=document.createElement('script')
   const timer=setTimeout(()=>{script.remove();reject(new Error('地图脚本超时'))},10000)
   script.onload=()=>{clearTimeout(timer);window.AMap ? resolve(window.AMap) : reject(new Error('地图脚本不可用'))}
   script.onerror=()=>{clearTimeout(timer);script.remove();reject(new Error('地图脚本加载失败'))}
   script.src='https://webapi.amap.com/maps?v=2.0&key='+encodeURIComponent(config.key)
   document.head.append(script)
  })
 })().catch(error=>{sdkPromise=undefined;throw error})
 return sdkPromise
}
export function amapFactory(container: HTMLElement, cities: {id:string;name:string}[]): MapFactory {
 return async(signal,onSelect)=>{
  container.dataset.stage='sdk'
  const sdk=await loadSDK()
  if(signal.aborted) throw new Error('cancelled')
  const map=new sdk.Map(container,{zoom:3,center:[104,30],viewMode:'2D',scrollWheel:false,animateEnable:false})
  const handlers: Array<()=>void>=[]
  let destroyed=false
  const destroy=()=>{if(destroyed)return;destroyed=true;handlers.forEach(remove=>remove());map.destroy()}
  signal.addEventListener('abort',destroy,{once:true})
  handlers.push(()=>signal.removeEventListener('abort',destroy))
  try {
   container.dataset.stage='tiles'
   await abortable(new Promise<void>((resolve,reject)=>{
    const complete=()=>{clearTimeout(timer);resolve()}
    const timer=setTimeout(()=>reject(new Error('地图未完成加载')),10000)
    map.on('complete',complete);handlers.push(()=>{clearTimeout(timer);map.off('complete',complete)})
   }),signal)
   const national=()=>map.setBounds(new sdk.Bounds([73,3],[135,54]),true,[40,30,40,30])
   national()
   container.dataset.stage='plugin'
   await abortable(new Promise<void>(resolve=>sdk.plugin(['AMap.DistrictSearch'],resolve)),signal)
   container.dataset.stage='coordinates'
   const entries: {id:string;name:string;center:number[]}[]=[]
   for(const city of cities) { if(signal.aborted) throw new Error('cancelled'); entries.push(await abortable(new Promise<{id:string;name:string;center:number[]}>((resolve,reject)=>{
    const cached=centers.get(city.id)
    if(cached) { resolve({...city,center:cached}); return }
    new sdk.DistrictSearch({subdistrict:0,extensions:'base'}).search(codes[city.id]!, (status,result)=>{
     const district=result?.districtList?.[0]; const center=district?.center
     if(status!=='complete' || !center || district.adcode!==codes[city.id]) { container.dataset.timeout=String(/timeout|超时/i.test(String(result?.info))); container.dataset.failure=['complete','error','no_data'].includes(status) ? status : 'unknown'; return reject(new Error('城市坐标服务未通过验证')) }
     const point=[center.lng,center.lat]
     if(!point.every(Number.isFinite)) return reject(new Error('坐标格式无效'))
     centers.set(city.id,point)
     resolve({...city,center:point})
    })
   }),signal)) }
   if(signal.aborted || destroyed) throw new Error('cancelled')
   const buttons=new Map<string,HTMLButtonElement>()
   const markers=entries.map(city=>{
    const button=document.createElement('button');button.type='button';button.textContent=city.name;button.title=city.name+' · 查看居住文化';button.className='amap-city-button';button.setAttribute('aria-label','地图城市：'+city.name)
    const select=()=>onSelect(city.id);button.addEventListener('click',select);handlers.push(()=>button.removeEventListener('click',select));buttons.set(city.id,button)
    return new sdk.Marker({position:city.center,content:button,anchor:city.id==='shanghai'?'top-left':city.id==='suzhou'?'bottom-right':'bottom-center'})
   })
   map.add(markers)
   const controls=document.createElement('div');controls.className='map-controls'
   for(const label of ['全国视角','放大','缩小'] as const){const button=document.createElement('button');button.type='button';button.textContent=label;const handler=()=>label==='全国视角' ? national() : map.setZoom(map.getZoom()+(label==='放大'?1:-1),true);button.addEventListener('click',handler);handlers.push(()=>button.removeEventListener('click',handler));controls.append(button)}
   container.append(controls);handlers.push(()=>controls.remove())
   container.dataset.stage='ready'
   let initial=true
   const adapter:CityMapAdapter={destroy,selectCity(id,animate){
    buttons.forEach((button,key)=>button.setAttribute('aria-pressed',String(key===id)))
    const city=entries.find(city=>city.id===id)
    if(initial){initial=false;return}
    if(city) map.setZoomAndCenter(7,city.center,!animate)
   }}
   return adapter
  } catch(error){destroy();throw error}
 }
}
