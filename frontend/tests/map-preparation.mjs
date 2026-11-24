// Injected adapters only: these assertions do NOT verify a real map SDK.
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'
const require=createRequire(import.meta.url)
const {chromium}=require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const browser=await chromium.launch({channel:'msedge',headless:true})
try {
 const page=await browser.newPage()
 const external=[]
 page.on('request',request=>{if(!['127.0.0.1','localhost'].includes(new URL(request.url()).hostname)) external.push(request.url())})
 await page.goto((process.env.UI_BASE_URL || 'http://127.0.0.1:5174')+'/explore')
 await page.locator('.city-choice').first().waitFor()
 const result=await page.evaluate(async()=>{
  const {createMapSession}=await import('/src/data/mapSession.ts')
  const {useCitySelection}=await import('/src/data/useCitySelection.ts')
  const {ref,effectScope}=await import('/node_modules/.vite/deps/vue.js')
  const cities=await (await fetch('/data/explore-cities.json')).json()
  const scope=effectScope(); const state=scope.run(()=>useCitySelection(ref(cities.cities)))
  const check=(value,message)=>{if(!value) throw new Error(message)}
  state.query.value='Shanghai'; check(state.selectedId.value==='shanghai','search selection')
  state.selectCity('beijing'); check(state.query.value==='' && state.selected.value.id==='beijing','external selection clears filter')
  check(!state.selectCity('unknown') && state.selectedId.value==='beijing','invalid selection')
  state.query.value='no-match'; check(state.selectedId.value==='' && !state.selected.value,'empty state')
  let status, destroyCount=0, calls=[], callback
  const session=createMapSession(value=>status=value,id=>state.selectCity(id))
  await session.start(); check(status==='unconfigured','no factory')
  session.selectCity('shanghai',true)
  await session.start(async(signal,onSelect)=>{callback=onSelect;return {selectCity:(id,animate)=>calls.push([id,animate]),destroy:()=>destroyCount++}})
  check(status==='ready' && calls[0][0]==='shanghai' && calls[0][1]===false,'selection replay/reduced motion')
  callback('lijiang'); check(state.selectedId.value==='lijiang','adapter callback')
  session.dispose(); callback('beijing'); check(state.selectedId.value==='lijiang' && destroyCount===1,'late events ignored')
  await session.start(async()=>{throw new Error('fixture')}); check(status==='error','failure state')
  let finish
  const pending=session.start(()=>new Promise(resolve=>{finish=resolve}),10)
  await new Promise(resolve=>setTimeout(resolve,30)); check(status==='error','timeout')
  finish({selectCity:()=>{},destroy:()=>destroyCount++}); await pending
  check(destroyCount===2 && status==='error','late instance destroyed')
  await session.start(async()=>({selectCity:()=>{},destroy:()=>destroyCount++})); check(status==='ready','retry')
  session.dispose(); scope.stop(); return true
 })
 assert.equal(result,true); assert.deepEqual(external,[])
 console.log('PASS map preparation: shared selection, invalid/empty, unconfigured/no external requests, injected adapter lifecycle, failure/timeout/retry, late cleanup, reduced motion. NO real SDK tested.')
} finally {await browser.close()}
