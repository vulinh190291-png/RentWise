// Attribution audit: no SDK source inspection, credentials or request URLs logged.
import {createRequire} from 'node:module'
import {mkdir,writeFile} from 'node:fs/promises'
const require=createRequire(import.meta.url)
const {chromium}=require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const browser=await chromium.launch({channel:'msedge',headless:true})
const results=[]
const out=process.env.UI_SCREENSHOT_DIR
try {
 const page=await browser.newPage()
 for(const width of [320,390,1024,1440]) {
  await page.setViewportSize({width,height:900})
  await page.goto('http://127.0.0.1:5174/explore')
  await page.getByRole('button',{name:'打开交互地图'}).click()
  await page.waitForFunction(()=>document.querySelector('.canvas')?.dataset.stage==='ready')
  await page.waitForTimeout(12000)
  const nodes=await page.evaluate(()=>[...document.querySelectorAll('[class*=copyright], [class*=logo]')].map(el=>({tag:el.tagName,class:el.className,text:el.textContent,rect:{x:el.getBoundingClientRect().x,y:el.getBoundingClientRect().y,width:el.getBoundingClientRect().width,height:el.getBoundingClientRect().height},display:getComputedStyle(el).display,visibility:getComputedStyle(el).visibility})))
  results.push({kind:'application',width,nodes})
  if(out){await mkdir(out,{recursive:true});await page.locator('.canvas').screenshot({path:`${out}/attribution-app-${width}.png`})}
 }
 // A separate fulfilled HTML navigation prevents Vite/app scripts from injecting late CSS.
 await page.route('**/__map_attribution_audit',route=>route.fulfill({contentType:'text/html',body:'<!doctype html><html><head></head><body style="margin:0"><div id="map" style="height:360px"></div></body></html>'}))
 for(const width of [270,340,740]) {
  await page.setViewportSize({width:800,height:700})
  await page.goto('http://127.0.0.1:5174/__map_attribution_audit')
  await page.evaluate(w=>document.querySelector('#map').style.width=w+'px',width)
  await page.evaluate(async()=>{
   const config=await(await fetch('/map-local/config')).json()
   window._AMapSecurityConfig={serviceHost:location.origin+config.serviceHost}
   await new Promise((resolve,reject)=>{const s=document.createElement('script');s.src='https://webapi.amap.com/maps?v=2.0&key='+encodeURIComponent(config.key);s.onload=resolve;s.onerror=reject;document.head.append(s)})
   window.auditMap=new AMap.Map('map',{zoom:4,center:[113,34],viewMode:'2D'})
  })
  await page.waitForTimeout(15000)
  const nodes=await page.evaluate(()=>[...document.querySelectorAll('[class*=copyright], [class*=logo]')].map(el=>({class:el.className,text:el.textContent,width:el.getBoundingClientRect().width,display:getComputedStyle(el).display})))
  const projectStyleNodes=await page.locator('style[data-vite-dev-id]').count()
  if(projectStyleNodes!==0)throw new Error('Minimal comparison retained project styles')
  results.push({kind:'minimal-no-project-css',width,projectStyleNodes,nodes})
  if(out)await page.locator('#map').screenshot({path:`${out}/attribution-minimal-${width}.png`})
  await page.evaluate(()=>window.auditMap.destroy())
 }
 if(out)await writeFile(`${out}/attribution-audit.json`,JSON.stringify(results,null,2))
 const missing=results.filter(row=>row.kind==='application' && !row.nodes.some(node=>node.class==='amap-copyright' && /GS/.test(node.text))).map(row=>row.width)
 console.log(JSON.stringify({copyrightMissingAt:missing,minimalComparison:results.filter(row=>row.kind!=='application')}))
 if(missing.length){console.log('BLOCKED: native attribution is incomplete; map compliance acceptance is not passed.');process.exitCode=2}
}finally{await browser.close()}
