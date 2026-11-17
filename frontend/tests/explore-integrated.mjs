import assert from 'node:assert/strict'
import {createRequire} from 'node:module'
import {mkdir} from 'node:fs/promises'
const {chromium}=createRequire(import.meta.url)(process.env.PLAYWRIGHT_MODULE||'playwright')
const browser=await chromium.launch({channel:'msedge',headless:true})
const out=process.env.UI_SCREENSHOT_DIR
async function ready(page) {
 for(let attempt=0;attempt<3;attempt++) {
  await page.waitForFunction(()=>document.querySelector('.canvas')?.dataset.stage==='ready'||document.querySelector('.city-map [role=alert]'),null,{timeout:35000})
  if(await page.locator('.city-map [role=alert]').count()) {
   console.log('Real map load failed; retry attempt '+(attempt+1))
   if(attempt===2)throw new Error('Map failed after two retries')
   await page.getByRole('button',{name:'重试地图'}).click()
  } else return
 }
}
try {
 for(const width of [1440,1024,390,320].filter(w=>!process.env.INTEGRATED_MOBILE_ONLY||w<768)) {
  const page=await browser.newPage({viewport:{width,height:900}})
  await page.goto('http://127.0.0.1:5174/explore')
  await page.getByRole('button',{name:'打开交互地图'}).click()
  await ready(page)
  assert.equal(await page.locator('.amap-city-button').count(),8)
  for(const [id,name] of [['beijing','北京'],['shanghai','上海'],['suzhou','苏州']]) {
   if(width<768 && id!=='beijing') {
    await page.getByRole('searchbox').fill(name)
    await page.locator('.city-choice').click()
    await page.getByRole('button',{name:'返回城市列表'}).click()
   }
   await page.getByRole('button',{name:'地图城市：'+name,exact:true}).click()
   await page.getByRole('heading',{name,exact:true}).waitFor()
   await page.getByRole('link',{name:'阅读 '+name+' 的住宅故事 ↗'}).click()
   await page.waitForFunction(()=>document.querySelector('.story img')?.naturalWidth>0)
   assert.ok((await page.locator('figcaption').innerText()).includes('CC'))
   if(out){await mkdir(out,{recursive:true});await page.screenshot({path:`${out}/integrated-story-${id}-${width}.png`,fullPage:true})}
   await page.getByRole('link',{name:'← 返回地图与城市目录'}).click()
   await ready(page)
   await page.waitForFunction(id=>document.querySelector('.canvas')?.dataset.stage==='ready'&&document.querySelector('.canvas')?.dataset.selected===id,id,{timeout:60000})
   await page.goBack();await page.locator('#story-title').waitFor()
   await page.goForward();await ready(page)
  }
  await page.getByRole('searchbox').fill('Suzhou')
  await page.locator('.city-choice').click()
  await page.getByRole('link',{name:'阅读 苏州 的住宅故事 ↗'}).click()
  await page.getByRole('link',{name:'← 返回地图与城市目录'}).click()
  await ready(page)
  assert.equal(await page.getByRole('searchbox').inputValue(),'Suzhou')
  await page.getByRole('searchbox').fill('')
  for(const name of ['广州','厦门','丽江','黄山','哈尔滨']) {
   await page.locator('.city-choice').filter({hasText:name}).click()
   assert.equal(await page.getByRole('link',{name:/阅读 .* 的住宅故事/}).count(),0)
   await page.getByRole('button',{name:'返回城市列表'}).click()
  }
  await page.getByRole('button',{name:'全国视角',exact:true}).click();await page.waitForTimeout(3000)
  assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false)
  if(out)await page.screenshot({path:`${out}/integrated-map-${width}.png`,fullPage:true})
  await page.close();console.log('PASS integrated map/story navigation, history, search restoration, five basic profiles: '+width)
 }
}finally{await browser.close()}
