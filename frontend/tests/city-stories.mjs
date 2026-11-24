import assert from 'node:assert/strict'
import {createRequire} from 'node:module'
import {mkdir} from 'node:fs/promises'
const require=createRequire(import.meta.url)
const {chromium}=require(process.env.PLAYWRIGHT_MODULE||'playwright')
const browser=await chromium.launch({channel:'msedge',headless:true})
const out=process.env.UI_SCREENSHOT_DIR
try{const page=await browser.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message))
for(const width of [1440,1024,390,320])for(const id of ['beijing','shanghai','suzhou']){
 await page.setViewportSize({width,height:900});await page.goto('http://127.0.0.1:5174/explore/stories/'+id)
 await page.locator('#story-title').waitFor();assert.equal(await page.locator('.reading section').count(),5)
 for(const a of await page.locator('.story-sources a').all())assert.ok((await a.getAttribute('href')).startsWith('https://'))
 await page.waitForFunction(()=>document.querySelector('.story img')?.naturalWidth>0)
 assert.ok(await page.locator('.story img').evaluate(img=>Math.abs(img.clientWidth/img.clientHeight-img.naturalWidth/img.naturalHeight)<0.01))
 assert.ok((await page.locator('figcaption').innerText()).includes('CC'))
 assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false)
 if(out){await mkdir(out,{recursive:true});await page.screenshot({path:`${out}/story-${id}-${width}.png`,fullPage:true})}
}
await page.goto('http://127.0.0.1:5174/explore/stories/beijing');await page.getByRole('link',{name:'下一篇 · 上海'}).click();await page.waitForFunction(()=>document.activeElement?.id==='story-title');await page.getByRole('link',{name:'上一篇 · 北京'}).click()
await page.getByRole('link',{name:'下一篇 · 上海'}).click();await page.getByRole('link',{name:'下一篇 · 苏州'}).click();await page.waitForFunction(()=>document.activeElement?.id==='story-title');assert.equal(await page.getByRole('link',{name:/下一篇/}).count(),0);await page.getByRole('link',{name:'上一篇 · 上海'}).click()
await page.route('**/images/stories/*.jpg',r=>r.abort())
for(const id of ['beijing','shanghai','suzhou']){await page.goto('http://127.0.0.1:5174/explore/stories/'+id);await page.getByText('摄影暂时无法加载').waitFor();await page.getByRole('heading',{name:'资料来源与阅读边界'}).waitFor();assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false)}
await page.goto('http://127.0.0.1:5174/explore/stories/unknown');await page.getByText('故事尚未发布').waitFor()
await page.getByRole('link',{name:'← 返回城市目录'}).click();await page.locator('.city-choice').first().waitFor();assert.equal(await page.locator('.city-choice').count(),8)
await page.locator('.city-choice').first().click();await page.getByRole('link',{name:'阅读 北京 的住宅故事 ↗'}).click();await page.locator('#story-title').waitFor()
assert.deepEqual(errors,[]);console.log('PASS stories: 3 cities, 4 viewports, source hrefs, three licensed images with original aspect ratios, failed image, navigation/focus, unknown route, directory entry.')
}finally{await browser.close()}
