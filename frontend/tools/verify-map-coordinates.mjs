import { createRequire } from 'node:module'
import { writeFile } from 'node:fs/promises'
const require=createRequire(import.meta.url)
const {chromium}=require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const browser=await chromium.launch({channel:'msedge',headless:true})
try {
 const page=await browser.newPage()
 await page.goto('http://127.0.0.1:5174/explore')
 const cities=await page.evaluate(async()=>{
  const config=await (await fetch('/map-local/config')).json()
  window._AMapSecurityConfig={serviceHost:location.origin+config.serviceHost}
  await new Promise((resolve,reject)=>{const script=document.createElement('script');script.src='https://webapi.amap.com/maps?v=2.0&key='+encodeURIComponent(config.key);script.onload=resolve;script.onerror=()=>reject(new Error('SDK unavailable'));document.head.append(script)})
  await new Promise(resolve=>AMap.plugin(['AMap.DistrictSearch'],resolve))
  const codes={beijing:'110000',shanghai:'310000',guangzhou:'440100',suzhou:'320500',xiamen:'350200',lijiang:'530700',huangshan:'341000',harbin:'230100'}
  const rows=[]
  for(const [id,adcode] of Object.entries(codes)) {
   let point
   for(let attempt=0;attempt<3 && !point;attempt++) {
    point=await new Promise(resolve=>{
     const timer=setTimeout(()=>resolve(null),15000)
     new AMap.DistrictSearch({subdistrict:0,extensions:'base'}).search(adcode,(status,result)=>{clearTimeout(timer);const d=result?.districtList?.[0];resolve(status==='complete' && d?.adcode===adcode ? [d.center.lng,d.center.lat] : null)})
    })
   }
   if(!point || !point.every(Number.isFinite)) throw new Error('Coordinate verification incomplete')
   rows.push({id,adcode,position:point})
  }
  return rows
 })
 await writeFile(new URL('../src/data/cityCoordinates.json',import.meta.url),JSON.stringify({version:'0.2.1',coordinateSystem:'GCJ-02',meaning:'AMap administrative district center; not a dwelling location',source:'AMap JS API 2.0 DistrictSearch; subdistrict=0; extensions=base',sourceUrl:'https://lbs.amap.com/api/javascript-api-v2/guide/services/district-search',checkedAt:new Date().toISOString().slice(0,10),cities},null,2)+'\n')
 console.log('Verified and saved 8 official district centers; no credentials saved.')
}finally{await browser.close()}
