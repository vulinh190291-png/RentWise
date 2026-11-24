import assert from 'node:assert/strict'
import {createRequire} from 'node:module'
import {mkdir} from 'node:fs/promises'
const {chromium}=createRequire(import.meta.url)(process.env.PLAYWRIGHT_MODULE||'playwright')
const browser=await chromium.launch({channel:'msedge',headless:true})
const base=process.env.UI_BASE_URL||'http://127.0.0.1:5181',out=process.env.UI_SCREENSHOT_DIR
try{const page=await browser.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message))
for(const width of [1440,1024,390,320]){
 await page.setViewportSize({width,height:900});await page.goto(base+'/living');await page.locator('h1').waitFor()
 assert.equal(await page.locator('.living-row').count(),3)
 for(const name of ['居住空间','生活方式','居住好物']){await page.getByRole('button',{name,exact:true}).click();assert.equal(await page.locator('.living-row').count(),1);assert.equal(await page.getByRole('button',{name,exact:true}).getAttribute('aria-pressed'),'true')}
 await page.getByRole('button',{name:'全部',exact:true}).focus();await page.keyboard.press('Enter');assert.equal(await page.locator('.living-row').count(),3)
 await page.locator('img').evaluate(e=>e.decode());assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false)
 if(out){await mkdir(out,{recursive:true});await page.screenshot({path:`${out}/living-${width}.png`,fullPage:true})}
 for(const slug of ['room-for-everyday','a-moment-outside','objects-that-stay']){
  await page.goto(base+'/living/'+slug);await page.getByRole('heading',{name:'署名、来源与阅读边界'}).waitFor()
  assert.equal(await page.locator('.living-reading section').count(),4)
  if(slug==='objects-that-stay')await page.getByText('专题摄影待补充',{exact:false}).waitFor();else{await page.locator('img').evaluate(e=>e.decode());assert.equal(await page.locator('figcaption a').count(),2)}
  assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false)
  if(out)await page.screenshot({path:`${out}/${slug}-${width}.png`,fullPage:true})
  await page.getByRole('link',{name:'← 返回 Living 目录'}).click();await page.waitForURL('**/living')
 }
}
await page.goto(base+'/living/missing');await page.getByRole('heading',{name:'专题尚未发布'}).waitFor()
await page.goto(base+'/');await page.getByRole('navigation',{name:'首页导航'}).getByRole('link',{name:'Living',exact:true}).click();await page.waitForURL('**/living')
await page.route('**/images/**',r=>r.abort());await page.goto(base+'/living/room-for-everyday');await page.getByText('摄影暂时无法加载，文字内容仍可阅读。').waitFor();await page.getByRole('heading',{name:'先走一遍，再决定摆放'}).waitFor()
assert.deepEqual(errors,[]);console.log('PASS Living: 4 widths, categories/keyboard, 3 articles, credits/placeholder, missing slug, home navigation, image fallback')
}finally{await browser.close()}
