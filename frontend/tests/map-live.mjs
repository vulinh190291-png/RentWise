// Real service observation. Do not print request URLs, browser logs or key values.
import assert from 'node:assert/strict'
import {createRequire} from 'node:module'
import {mkdir,writeFile} from 'node:fs/promises'
const require=createRequire(import.meta.url)
const {chromium}=require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const browser=await chromium.launch({channel:'msedge',headless:true})
const out=process.env.UI_SCREENSHOT_DIR
const results=[]
try {
 const page=await browser.newPage()
 page.on('response',async r=>{if(r.url().includes('/_AMapService/')){try {const body=await r.text();const code=body.match(/infocode["':\s]+(\d{5})/);console.log(JSON.stringify({proxyStatus:r.status(),serviceCode:code?.[1] || 'none'}))}catch{}}})
 await page.goto('http://127.0.0.1:5174/explore')
 for(const [width,height] of [[1440,900],[390,844]]) {
  await page.setViewportSize({width,height})
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
  await page.waitForTimeout(5000)
  if(out){await mkdir(out,{recursive:true});await page.screenshot({path:`${out}/map-live-${width}.png`,fullPage:true})}
  console.log(JSON.stringify(results.at(-1)))
  assert.equal(markers,8,'real SDK must expose eight city nodes');assert.equal(securityCodeInBrowser,false)
  assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false)
  if(markers===8){await page.getByRole('button',{name:'地图城市：哈尔滨',exact:true}).click();await page.getByRole('heading',{name:'哈尔滨',exact:true}).waitFor()}
  await page.getByRole('button',{name:'返回城市列表'}).click()
  await page.getByRole('searchbox').fill('Suzhou')
  await page.locator('.city-choice').click()
  await page.waitForFunction(()=>document.querySelector('[aria-label="地图城市：苏州"]')?.getAttribute('aria-pressed')==='true')
  await page.getByRole('button',{name:'全国视角',exact:true}).click()
  const node=page.getByRole('button',{name:'地图城市：哈尔滨',exact:true});await node.focus();await page.keyboard.press('Enter')
  await page.getByRole('heading',{name:'哈尔滨',exact:true}).waitFor()
  await page.waitForTimeout(5000)
  if(out)await page.screenshot({path:`${out}/map-detail-${width}.png`,fullPage:true})
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
 if(out)await writeFile(`${out}/map-live-result.json`,JSON.stringify(results,null,2))
 console.log(JSON.stringify(results))
}finally{await browser.close()}

