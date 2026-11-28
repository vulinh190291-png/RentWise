import assert from 'node:assert/strict'
import {createRequire} from 'node:module'
import {readFile,mkdir} from 'node:fs/promises'
const {chromium}=createRequire(import.meta.url)(process.env.PLAYWRIGHT_MODULE||'playwright')
const data=JSON.parse(await readFile(new URL('../public/data/explore-cities.json',import.meta.url),'utf8')).cities
const browser=await chromium.launch({channel:'msedge',headless:true}),page=await browser.newPage()
const base=process.env.UI_BASE_URL||'http://127.0.0.1:5182',out=process.env.UI_SCREENSHOT_DIR
const codes=['geography','housing','cost','transport','employment','culture'],names=['地理环境','住房建筑','居住成本','交通通勤','就业发展','文化生活']
const value=(id,code)=>{const c=data.find(c=>c.id===id);if(!['beijing','shanghai','suzhou'].includes(id)||!['geography','housing','culture'].includes(code))return null;const s=c.sources[code==='geography'?0:1];return {textValue:c[code],numericValue:null,period:'UNSPECIFIED',unit:'定性描述',scope:code==='geography'?'城市背景':c.housingTitle,method:'官方资料摘要',sourceTitle:s.title,sourceUrl:s.url,checkedAt:s.checkedAt}}
try{
const errors=[];page.on('pageerror',e=>errors.push(e.message))
await page.route('**/api/content/**',async r=>{const u=new URL(r.request().url());if(u.pathname.endsWith('/cities'))return r.fulfill({json:data.map(c=>({slug:c.id,name:c.name}))});const l=u.searchParams.get('left'),right=u.searchParams.get('right');return r.fulfill({json:{metrics:codes.map((code,i)=>({code,name:names[i],kind:[2,3,4].includes(i)?'NUMBER':'TEXT',left:value(l,code),right:value(right,code),status:value(l,code)&&value(right,code)?'INCOMPARABLE':'MISSING'}))}})})
for(const width of [1440,1024,390,320]){await page.setViewportSize({width,height:900});await page.goto(base+'/compare');await page.locator('.compare-section').last().waitFor();assert.equal(await page.locator('.compare-section').count(),6);for(const section of await page.locator('.compare-section').all())await section.scrollIntoViewIfNeeded();for(const img of await page.locator('img').all())await img.evaluate(e=>e.decode());await page.evaluate(()=>scrollTo(0,0));assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false);if(out){await mkdir(out,{recursive:true});await page.screenshot({path:`${out}/compare-fixture-${width}.png`,fullPage:true})}}
await page.getByLabel('城市二').selectOption('suzhou');await page.waitForFunction(()=>document.querySelectorAll('.compare-section').length===0);await page.getByRole('button',{name:'查看生活图鉴'}).click();await page.locator('.compare-section').last().waitFor();assert.equal(await page.locator('a[href="/explore/stories/suzhou"]').count(),1)
await page.getByLabel('城市二').selectOption('guangzhou');await page.getByRole('button',{name:'查看生活图鉴'}).click();await page.locator('.compare-section').last().waitFor();assert.equal(await page.locator('.number').count(),0)
await page.route('**/api/content/**',r=>r.fulfill({status:503,json:{}}));await page.getByRole('button',{name:'查看生活图鉴'}).click();await page.getByRole('alert').waitFor();await page.getByRole('button',{name:'重试'}).waitFor()
await page.goto(base+'/');await page.getByRole('navigation',{name:'首页导航'}).getByRole('link',{name:'Compare',exact:true}).click();await page.waitForURL('**/compare')
await page.emulateMedia({reducedMotion:'reduce'});assert.equal(await page.evaluate(()=>matchMedia('(prefers-reduced-motion:reduce)').matches),true)
assert.deepEqual(errors,[]);console.log('PASS Compare fixture UI: four widths, six dimensions, selection/stale-clear, missing data, failure/retry control, story and home links, reduced motion. NOT real service acceptance.')
}finally{await browser.close()}
