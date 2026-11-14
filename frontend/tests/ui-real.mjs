// Opt-in integration: creates one dedicated MySQL test user through the real API.
// Never logs or stores passwords/tokens. Requires running v0.4 services.
import { createRequire } from 'node:module'
import { randomBytes } from 'node:crypto'
import { writeFile, mkdir } from 'node:fs/promises'
import assert from 'node:assert/strict'
const require=createRequire(import.meta.url)
const {chromium}=require(process.env.PLAYWRIGHT_MODULE||'playwright')
const browser=await chromium.launch({channel:'msedge',headless:true})
const page=await browser.newPage()
const base=process.env.UI_BASE_URL||'http://127.0.0.1:5173'
const out=process.env.UI_EVIDENCE_DIR
if(!out) throw Error('Set UI_EVIDENCE_DIR to a persistent local directory')
await mkdir(out,{recursive:true})
const username=`ui01_${Date.now()}`
const password=`Ui!${randomBytes(18).toString('hex')}`
const result={username, mode:'REAL v0.4 HTTP through Vite/Gateway; no fulfilled/mocked API responses',checks:[],requests:[]}
page.on('response',res=>{if(new URL(res.url()).pathname.startsWith('/api/'))result.requests.push({path:new URL(res.url()).pathname,status:res.status()})})
const check=name=>{result.checks.push(name);console.log('PASS',name)}
try {
 await page.goto(base+'/register')
 await page.getByRole('button',{name:'创 建 账 户'}).click()
 await page.getByText('请输入用户名',{exact:true}).waitFor();check('register empty validation')
 await page.getByLabel('用户名',{exact:true}).fill(username)
 await page.getByLabel('密码',{exact:true}).fill(password)
 await page.getByLabel('确认密码',{exact:true}).fill(password)
 const registered=page.waitForResponse(r=>r.url().endsWith('/api/auth/register'))
 await page.getByRole('button',{name:'创 建 账 户'}).click()
 const registration=await registered;assert.equal(registration.status(),200);assert((await registration.json()).success)
 await page.waitForURL('**/login');check('real registration successful')
 // Duplicate account is a real backend validation failure.
 const duplicate=await page.request.post(base+'/api/auth/register',{data:{username,password}})
 assert.equal(duplicate.status(),409);check('real duplicate registration 409')
 await page.locator('#username').fill(username);await page.locator('#password').fill('deliberately-wrong-password')
 const rejected=page.waitForResponse(r=>r.url().endsWith('/api/auth/login'))
 await page.getByRole('button',{name:'登录，继续学习'}).click()
 assert.equal((await rejected).status(),401)
 await page.locator('.login-error').waitFor();check('real wrong password 401 and visible error')
 await page.screenshot({path:`${out}/real-login-error.png`,fullPage:true})
 let loginRequests=0
 page.on('request',r=>{if(r.url().endsWith('/api/auth/login'))loginRequests++})
 await page.locator('#password').fill(password)
 await page.locator('form').evaluate(form=>{form.dispatchEvent(new Event('submit',{bubbles:true,cancelable:true}));form.dispatchEvent(new Event('submit',{bubbles:true,cancelable:true}))})
 await page.waitForURL('**/dashboard');assert.equal(loginRequests,1);check('real login and duplicate-submit exclusion')
 await page.getByRole('heading',{name:`欢迎回来，${username}`}).waitFor()
 assert(await page.evaluate(()=>!!sessionStorage.getItem('rentwise_access_token')?.match(/^[^.]+\.[^.]+\.[^.]+$/)))
 check('JWT stored in sessionStorage and real users/me authenticated')
 // Exercise existing diagnosis APIs with this dedicated test account only.
 const token=await page.evaluate(()=>sessionStorage.getItem('rentwise_access_token'))
 const headers={Authorization:`Bearer ${token}`}
 const call=async(path,data)=>{const res=await page.request.post(base+path,{headers,data});assert.equal(res.status(),200,`API ${path}`);const body=await res.json();assert(body.success);return body.data}
 const session=await call('/api/diagnosis/start')
 assert.equal(session.cases.length,10)
 for(const item of session.cases)await call(`/api/diagnosis/${session.sessionId}/answers`,{caseId:item.caseId,selectedClarify:true})
 await call(`/api/diagnosis/${session.sessionId}/finish`)
 const profile=await page.request.get(base+'/api/profile/me',{headers});assert.equal(profile.status(),200);assert.equal((await profile.json()).data.topics.length,5)
 check('real 10-question diagnosis initializes five-topic Profile')
 await page.reload();await page.getByRole('heading',{name:`欢迎回来，${username}`}).waitFor();check('refresh restores authenticated user')
 await page.getByRole('heading',{name:'知识主题掌握度'}).waitFor();await page.screenshot({path:`${out}/real-dashboard.png`,fullPage:true})
 await page.getByRole('button',{name:'开始学习诊断',exact:true}).click();await page.waitForURL('**/diagnosis');await page.getByRole('heading',{name:'租赁风险知识诊断'}).waitFor();check('diagnosis entry')
 for(const [button,path] of [['查看完整学习画像','results'],['开始自适应训练','training'],['开始阶段测评','assessment']]){
  await page.goto(base+'/dashboard');await page.getByRole('button',{name:button,exact:true}).click();await page.waitForURL(`**/${path}`);check(`${path} entry navigates`)
 }
 await page.goto(base+'/dashboard');await page.getByRole('button',{name:'退出登录'}).click();await page.waitForURL('**/login')
 assert.equal(await page.evaluate(()=>sessionStorage.getItem('rentwise_access_token')),null);check('logout clears authentication')
 await page.goto(base+'/dashboard');await page.waitForURL('**/login');check('unauthenticated protected redirect')
 // A transport fault injection, not an actual server shutdown; never stop user's services.
 await page.route('**/api/auth/login',route=>route.abort('connectionrefused'))
 await page.locator('#username').fill(username);await page.locator('#password').fill(password)
 await page.getByRole('button',{name:'登录，继续学习'}).click();await page.locator('.login-error').waitFor()
 assert(await page.getByRole('button',{name:'登录，继续学习'}).isEnabled());check('injected network outage is visible and submit recovers')
 result.status='passed (real authentication, diagnosis and learning-entry checks; pre-diagnosis status is not asserted by this UI test)'
} catch(e){result.status='failed';result.failure=String(e);throw e}
finally{await writeFile(`${out}/real-results.json`,JSON.stringify(result,null,2));await browser.close()}
