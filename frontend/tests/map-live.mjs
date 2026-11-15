// Real service observation. Do not print request URLs, browser logs or key values.
import assert from 'node:assert/strict'
import {createRequire} from 'node:module'
import {mkdir,writeFile} from 'node:fs/promises'
const require=createRequire(import.meta.url)
const {chromium}=require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const browser=await chromium.launch({channel:'msedge',headless:true})
const out=process.env.UI_SCREENSHOT_DIR
const results=[]
const servicePaths=new Set()
let districtRequests=0
try {
 const page=await browser.newPage()
 page.on('request',r=>{const u=new URL(r.url());if(u.pathname.includes('/_AMapService/'))servicePaths.add(u.pathname);if(u.pathname.includes('/config/district'))districtRequests++})
 page.on('response',async r=>{if(r.url().includes('/_AMapService/')){try {const body=await r.text();const code=body.match(/infocode["':\s]+(\d{5})/);console.log(JSON.stringify({proxyStatus:r.status(),serviceCode:code?.[1] || 'none'}))}catch{}}})
 await page.goto('http://127.0.0.1:5174/explore')
 for(const [width,height] of [[1440,900],[1024,768],[390,844],[320,740]].filter(([w])=>!process.env.MAP_WIDTH || Number(process.env.MAP_WIDTH)===w)) {
  await page.setViewportSize({width,height})
  if(await page.getByRole('button',{name:'返回城市列表'}).isVisible())await page.getByRole('button',{name:'返回城市列表'}).click()
  await page.getByRole('button',{name:'打开交互地图'}).click()
  await page.waitForFunction(()=>document.querySelector('.amap-city-button') || document.querySelector('.city-map [role=alert]'),{},{timeout:70000})
  await page.waitForFunction(()=>document.querySelector('.canvas')?.dataset.stage==='ready' || document.querySelector('.city-map [role=alert]'),{},{timeout:70000})
  let retries=0
  if(await page.locator('.city-map [role=alert]').count()) {
   retries++;await page.getByRole('button',{name:'重试地图'}).click()
   await page.waitForFunction(()=>document.querySelector('.canvas')?.dataset.stage==='ready' || document.querySelector('.city-map [role=alert]'),{},{timeout:70000})
  }
  const markers=await page.locator('.amap-city-button').count()
  const sdk=await page.evaluate(()=>Boolean(window.AMap))
  const securityCodeInBrowser=await page.evaluate(()=>Boolean(window._AMapSecurityConfig?.securityJsCode))
  const stage=await page.locator('.canvas').getAttribute('data-stage')
  const failure=await page.locator('.canvas').getAttribute('data-failure')
  results.push({width,retries,sdk,markers,stage,failure,securityCodeInBrowser,status:markers===8?'ready':'blocked'})
  // Allow remote tiles to paint after bounds/viewport changes before visual review.
  await page.waitForTimeout(10000)
  if(out){await mkdir(out,{recursive:true});await page.screenshot({path:`${out}/map-live-${width}.png`,fullPage:true})}
  console.log(JSON.stringify(results.at(-1)))
  assert.equal(markers,8,'real SDK must expose eight city nodes');assert.equal(securityCodeInBrowser,false)
  assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false)
  assert.equal(await page.locator('.city-choice').count(),8)
  if(width>=768){const layout=await page.evaluate(()=>{const map=document.querySelector('.city-map').getBoundingClientRect(),detail=document.querySelector('.city-detail').getBoundingClientRect();return {ratio:map.width/(map.width+detail.width),sameRow:Math.abs(map.top-detail.top)<2}});assert.equal(layout.sameRow,true);assert.ok(layout.ratio>=0.60 && layout.ratio<=0.65)}
  const overviewZoom=Number(await page.locator('.canvas').getAttribute('data-zoom'));assert.ok(overviewZoom>=3 && overviewZoom<=6)

  if(markers===8){await page.getByRole('button',{name:'地图城市：哈尔滨',exact:true}).click();await page.getByRole('heading',{name:'哈尔滨',exact:true}).waitFor()}
  await page.getByRole('button',{name:'返回城市列表'}).click()
  assert.equal(await page.evaluate(()=>document.activeElement?.getAttribute('data-city-id')),'harbin')
  await page.getByRole('searchbox').fill('Suzhou')
  await page.locator('.city-choice').click()
  await page.waitForFunction(()=>document.querySelector('[aria-label="地图城市：苏州"]')?.getAttribute('aria-pressed')==='true')
  if(width<768)await page.getByRole('button',{name:'返回城市列表'}).click()
  await page.getByRole('button',{name:'全国视角',exact:true}).click()
  console.log(JSON.stringify({width,overviewZoom,returnedZoom:Number(await page.locator('.canvas').getAttribute('data-zoom'))}))
  await page.waitForFunction(()=>{
   const canvas=document.querySelector('.canvas').getBoundingClientRect()
   return [...document.querySelectorAll('.amap-city-button')].every(node=>{const r=node.getBoundingClientRect();return r.left>=canvas.left && r.right<=canvas.right && r.top>=canvas.top && r.bottom<=canvas.bottom})
  })
  assert.ok(Number(await page.locator('.canvas').getAttribute('data-zoom'))<=6)

  const node=page.getByRole('button',{name:'地图城市：哈尔滨',exact:true});await node.focus();await page.keyboard.press('Enter')
  await page.getByRole('heading',{name:'哈尔滨',exact:true}).waitFor()
  await page.waitForTimeout(10000)
  if(out)await page.screenshot({path:`${out}/map-detail-${width}.png`,fullPage:true})
  if(width<768)await page.getByRole('button',{name:'返回城市列表'}).click()
  for(const name of ['北京','上海','广州','苏州','厦门','丽江','黄山','哈尔滨']) {
   await page.locator('.city-choice').filter({hasText:name}).click()
   await page.getByRole('heading',{name,exact:true}).waitFor()
   await page.getByRole('button',{name:'返回城市列表'}).click()
  }
  await page.getByRole('button',{name:'使用城市列表'}).click()
  assert.equal(await page.locator('.amap-maps').count(),0)
  results.at(-1).interactions='node click, search/list selection, keyboard, national view, unmount'

 }
 const failurePage=await browser.newPage()
 await failurePage.route('**/map-local/config',route=>route.fulfill({status:503,body:'unavailable'}))
 await failurePage.goto('http://127.0.0.1:5174/explore')
 await failurePage.getByRole('button',{name:'打开交互地图'}).click()
 await failurePage.locator('.city-map [role=alert]').waitFor()
 await failurePage.getByRole('button',{name:'重试地图'}).click()
 await failurePage.locator('.city-map [role=alert]').waitFor()
 await failurePage.getByRole('button',{name:'使用城市列表'}).click()
 await failurePage.locator('.city-choice').first().click()
 await failurePage.getByRole('heading',{name:'北京',exact:true}).waitFor()
 results.push({scenario:'proxy unavailable (intercepted)',retryAndDirectory:true})
 await failurePage.close()
 assert.equal(districtRequests,0,'static centers must remove runtime district requests')
 results.push({servicePaths:[...servicePaths],districtRequests})
 if(out)await writeFile(`${out}/map-live-result.json`,JSON.stringify(results,null,2))
 console.log(JSON.stringify(results))
}finally{await browser.close()}
