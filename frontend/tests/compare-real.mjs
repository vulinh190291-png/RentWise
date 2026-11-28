import assert from 'node:assert/strict'
import {mkdir,writeFile} from 'node:fs/promises'
import {createRequire} from 'node:module'
const out=process.env.UI_SCREENSHOT_DIR;await mkdir(out,{recursive:true});const checks=[]
for(const port of [8084,8085]){
 const base=`http://127.0.0.1:${port}/api/content`
 const c=await fetch(base+'/cities');assert.equal(c.status,200);assert.equal((await c.json()).length,8)
 const d=await fetch(base+'/cities/suzhou');assert.equal(d.status,200);assert.equal((await d.json()).metrics.length,6)
 const r=await fetch(base+'/compare?left=beijing&right=shanghai');assert.equal(r.status,200);const body=await r.json();assert.equal(body.metrics.length,6);assert.equal(body.metrics.filter(x=>x.status==='MISSING').length,3)
 assert.equal((await fetch(base+'/compare?left=beijing&right=beijing')).status,400)
 assert.equal((await fetch(base+'/compare?left=unknown&right=shanghai')).status,404)
 assert.equal((await fetch(base+'/compare?left=beijing')).status,400)
 checks.push({port,status:'passed',body})
}
assert.deepEqual(checks[0].body,checks[1].body)
const {chromium}=createRequire(import.meta.url)(process.env.PLAYWRIGHT_MODULE);const browser=await chromium.launch({channel:'msedge',headless:true})
try{const page=await browser.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message))
for(const width of [1440,390]){await page.setViewportSize({width,height:900});await page.goto('http://127.0.0.1:5182/compare');await page.locator('.compare-section').last().waitFor();for(const s of await page.locator('.compare-section').all())await s.scrollIntoViewIfNeeded();for(const img of await page.locator('img').all())await img.evaluate(e=>e.decode());await page.waitForTimeout(450);await page.evaluate(()=>scrollTo(0,0));await page.screenshot({path:`${out}/compare-real-${width}.png`,fullPage:true})}
await page.getByLabel('城市二').selectOption('suzhou');await page.getByRole('button',{name:'查看生活图鉴'}).click();await page.locator('a[href="/explore/stories/suzhou"]').click();await page.locator('#story-title').waitFor();await page.goBack();await page.waitForURL('**/compare');assert.deepEqual(errors,[])
console.log('PASS REAL: both ports cities/detail/compare, identical results, 400/404, browser two widths, selection and story navigation')
}finally{await browser.close();await writeFile(`${out}/real-api.json`,JSON.stringify(checks,null,2))}
