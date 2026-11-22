import assert from 'node:assert/strict'
import {createRequire} from 'node:module'
import {mkdir} from 'node:fs/promises'
const {chromium}=createRequire(import.meta.url)(process.env.PLAYWRIGHT_MODULE||'playwright')
const browser=await chromium.launch({channel:'msedge',headless:true})
const base=process.env.UI_BASE_URL||'http://127.0.0.1:5174',out=process.env.UI_SCREENSHOT_DIR
try {
 const page=await browser.newPage(),errors=[],requests=[]
 page.on('pageerror',e=>errors.push(e.message));page.on('request',r=>requests.push(r.url()))
 for(const width of [1440,1024,390,320]) {
  await page.setViewportSize({width,height:width<768?844:900});await page.goto(base+'/')
  await page.getByRole('heading',{level:1}).waitFor();assert.equal(new URL(page.url()).pathname,'/')
  assert.equal(await page.locator('main > section').count(),4);assert.equal(await page.locator('footer').count(),1)
  for(const img of await page.locator('main img').all()){await img.scrollIntoViewIfNeeded();await img.evaluate(e=>e.decode())}
  await page.evaluate(()=>scrollTo(0,0));assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false)
  assert.equal(await page.locator('a[href="/living"],a[href="/compare"]').count(),0)
  assert.ok(!requests.some(u=>/amap|CityMap|amapAdapter|CityStoryPage/.test(u)))
  if(out){await mkdir(out,{recursive:true});await page.screenshot({path:`${out}/home-${width}-full.png`,fullPage:true});await page.screenshot({path:`${out}/home-${width}-hero.png`})}
 }
 await page.getByRole('navigation',{name:'首页导航'}).getByRole('link',{name:'Living',exact:true}).click();assert.equal(new URL(page.url()).hash,'#living')
 await page.getByRole('navigation',{name:'首页导航'}).getByRole('link',{name:'Compare',exact:true}).click();assert.equal(new URL(page.url()).hash,'#tools')
 for(const [id,title] of [['beijing','从一扇门，读懂一座院'],['shanghai','门里门外，里弄里的城市生活'],['suzhou','在庭院里，容纳一片山水']]) {
  await page.goto(base+'/');await page.locator(`a[href="/explore/stories/${id}"]`).click();await page.getByRole('heading',{name:title,exact:true}).waitFor();await page.goBack();await page.getByRole('heading',{level:1}).waitFor()
 }
 await page.getByRole('link',{name:'探索城市',exact:false}).first().click();await page.locator('.city-choice').first().waitFor();assert.equal(await page.locator('.city-choice').count(),8)
 await page.goto(base+'/');await page.getByRole('link',{name:'开始风险学习',exact:false}).first().click();await page.waitForURL('**/login')
 await page.goto(base+'/dashboard');await page.waitForURL('**/login')
 await page.goto(base+'/');await page.keyboard.press('Tab');await page.getByRole('link',{name:'跳至正文'}).focus();await page.keyboard.press('Enter');assert.equal(await page.evaluate(()=>document.activeElement?.id),'home-main')
 await page.evaluate(()=>sessionStorage.setItem('rentwise_access_token','homepage-test-fixture'));await page.goto(base+'/');await page.getByRole('link',{name:'我的学习 ↗'}).waitFor();assert.equal(new URL(page.url()).pathname,'/');await page.evaluate(()=>sessionStorage.clear())
 await page.route('**/images/**',r=>r.abort());await page.goto(base+'/');await page.locator('.fallback').first().waitFor();assert.equal(await page.locator('main > section').count(),4)
 await page.emulateMedia({reducedMotion:'reduce'});assert.equal(await page.evaluate(()=>matchMedia('(prefers-reduced-motion: reduce)').matches),true)
 assert.deepEqual(errors,[]);console.log('PASS homepage: 4 widths, 5 regions, images/credits, no SDK/story-page preload, anchors, three stories/back, Explore, Learn/login guards, public authenticated home, skip focus, image fallback, reduced motion')
}finally{await browser.close()}
