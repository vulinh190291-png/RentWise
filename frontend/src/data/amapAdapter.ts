import coordinateSource from './cityCoordinates.json'
import type { MapFactory, CityMapAdapter } from './mapSession'
// SDK structural boundary; no SDK code or credentials are included in the bundle.
type EventTarget = { on(name: string, handler: (...args: any[]) => void): void; off(name: string, handler: (...args: any[]) => void): void }
type MapInstance = EventTarget & { setFitView(markers: Marker[], immediately: boolean, padding: number[], maxZoom: number): void; getZoom(): number; setZoom(zoom: number, immediately?: boolean): void; add(markers: Marker[] | LabelLayer): void; destroy(): void; setZoomAndCenter(zoom: number, center: number[], immediately?: boolean): void }
type Marker = EventTarget & { setContent(content: HTMLElement): void }
type Label = EventTarget & { setRank(rank:number):void; setText(text:object):void }
type LabelLayer = { add(labels:Label[]):void; clear():void }
type SDK = {
 LabelsLayer: new (options:object) => LabelLayer
 LabelMarker: new (options:object) => Label
 Map: new (container: HTMLElement, options: object) => MapInstance
 Marker: new (options: object) => Marker
 plugin(names: string[], callback: () => void): void
}
declare global { interface Window { AMap?: SDK; _AMapSecurityConfig?: { serviceHost: string } } }
function abortable<T>(promise: Promise<T>, signal: AbortSignal): Promise<T> {
 return new Promise((resolve,reject)=>{
  const abort=()=>reject(new Error('cancelled'))
  if(signal.aborted){abort();return}
  signal.addEventListener('abort',abort,{once:true})
  promise.then(resolve,reject).finally(()=>signal.removeEventListener('abort',abort))
 })
}
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
   const entries=cities.map(city=>{
    const coordinate=coordinateSource.cities.find(row=>row.id===city.id)
    if(!coordinate) throw new Error('城市坐标尚未核验')
    return {...city,center:coordinate.position}
   })
   if(signal.aborted || destroyed) throw new Error('cancelled')
   const buttons=new Map<string,HTMLButtonElement>()
   const markers=entries.map(city=>{
    const button=document.createElement('button');button.type='button';button.dataset.cityId=city.id;button.title=city.name+' · 查看居住文化';button.className='amap-city-button';button.setAttribute('aria-label','地图城市：'+city.name)
    const select=()=>onSelect(city.id);button.addEventListener('click',select);handlers.push(()=>button.removeEventListener('click',select));buttons.set(city.id,button)
    return new sdk.Marker({position:city.center,content:button,anchor:'center'})
   })
   map.add(markers)
   // Native collision avoidance never changes geographic coordinates.
   const layer=new sdk.LabelsLayer({zooms:[2,20],zIndex:110,collision:true,allowCollision:true})
   map.add(layer)
   const labels=entries.map(city=>new sdk.LabelMarker({name:city.id,position:city.center,rank:1,text:{content:city.name,direction:'right',offset:[12,0],style:{fontSize:13,fillColor:'#242424',strokeColor:'#fff',strokeWidth:3}}}))
   layer.add(labels);handlers.push(()=>layer.clear())
   let selected=''
   const updateLabels=()=>{
    container.dataset.zoom=String(map.getZoom())
    labels.forEach((label,index)=>{
     const city=entries[index]!
     const active=city.id===selected
     // Sparse regional labels at overview zoom; all names eligible when zoomed in.
     const show=active || map.getZoom()>=5.5 || ['beijing','shanghai','guangzhou','harbin','lijiang'].includes(city.id)
     label.setRank(active ? 10000 : 10-index)
     label.setText({content:show ? city.name : '',direction:'right',offset:[12,0],style:{fontSize:active?15:13,fontWeight:active?'bold':'normal',fillColor:active?'#111':'#494640',strokeColor:'#fff',strokeWidth:4}})
    })
   }
   labels.forEach((label,index)=>{const click=()=>onSelect(entries[index]!.id);label.on('click',click);handlers.push(()=>label.off('click',click))})
   map.on('zoomend',updateLabels);handlers.push(()=>map.off('zoomend',updateLabels))
   const national=()=>{map.setFitView(markers,true,[72,56,40,80],6);updateLabels()}
   national()
   const controls=document.createElement('div');controls.className='map-controls'
   for(const label of ['全国视角','放大','缩小'] as const){const button=document.createElement('button');button.type='button';button.textContent=label;const handler=()=>label==='全国视角' ? national() : map.setZoom(map.getZoom()+(label==='放大'?1:-1),true);button.addEventListener('click',handler);handlers.push(()=>button.removeEventListener('click',handler));controls.append(button)}
   container.append(controls);handlers.push(()=>controls.remove())
   container.dataset.stage='ready'
   let initial=true
   const adapter:CityMapAdapter={destroy,selectCity(id,animate){
    selected=id;container.dataset.selected=id;updateLabels()
    buttons.forEach((button,key)=>button.setAttribute('aria-pressed',String(key===id)))
    const city=entries.find(city=>city.id===id)
    if(initial){initial=false;return}
    if(city) map.setZoomAndCenter(Math.min(7,Math.max(6,map.getZoom())),city.center,!animate)
   }}
   return adapter
  } catch(error){destroy();throw error}
 }
}
