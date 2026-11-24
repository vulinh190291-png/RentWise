// Explicit opt-in: creates ONE dedicated account and real diagnosis/training/assessment records.
// No mocked responses, DB access, secret files, or cleanup of any user data.
// This script only proves DB persistence once supplemented by read-only SQL evidence.
// Requires PLAYWRIGHT_MODULE, EVIDENCE_DIR, RENTWISE_REAL_TEST=1 and running v0.4 services.
import assert from 'node:assert/strict'
import { randomBytes } from 'node:crypto'
import { mkdir, writeFile } from 'node:fs/promises'
import path from 'node:path'
import { pathToFileURL } from 'node:url'

if (process.env.RENTWISE_REAL_TEST !== '1' || !process.env.PLAYWRIGHT_MODULE || !process.env.EVIDENCE_DIR) {
  throw new Error('Set RENTWISE_REAL_TEST=1, PLAYWRIGHT_MODULE and a persistent EVIDENCE_DIR; this test writes dedicated test records')
}
const { chromium } = await import(pathToFileURL(path.join(process.env.PLAYWRIGHT_MODULE, 'index.mjs')).href)
const out = process.env.EVIDENCE_DIR
await mkdir(out, { recursive: true })
const browser = await chromium.launch({ channel: 'msedge', headless: true })
const page = await browser.newPage()
page.setDefaultTimeout(15000)
const base = process.env.FRONTEND_URL || 'http://localhost:5173'
const gateway = process.env.GATEWAY_URL || 'http://127.0.0.1:8080'
const learning = process.env.LEARNING_URL || 'http://127.0.0.1:8082'
const username = `home_accept_${Date.now()}`
const password = `Test!${randomBytes(18).toString('hex')}`
const evidence = { pageErrors: [], consoleErrors: [], networkFailures: [], browser: browser.version(), time: new Date().toISOString(), mode: 'REAL HTTP, no mocks', username, checks: [], requests: [] }
const check = name => { evidence.checks.push(name); console.log('PASS', name) }
page.on('pageerror', error => evidence.pageErrors.push(error.name))
page.on('console', message => { if(message.type()==='error') evidence.consoleErrors.push(message.text().replace(/https?:\/\/[^\s]+/g,'[URL removed]')) })
page.on('requestfailed', request => evidence.networkFailures.push({path:new URL(request.url()).pathname,error:request.failure()?.errorText}))
page.on('response', response => { const p=new URL(response.url()).pathname;if(p.startsWith('/api/')) evidence.requests.push({path:p,status:response.status()}) })
let headers = {}
async function profileAt(origin, expectedStatus) {
  const res = await page.request.get(origin + '/api/profile/me', { headers })
  const body = await res.json()
  evidence.requests.push({ path: origin + '/api/profile/me', status: res.status(), body })
  assert.equal(res.status(), expectedStatus)
  return body
}
try {
  const initialRequests=[]
  page.on('request',r=>initialRequests.push(r.url()))
  await page.goto(base+'/')
  await page.getByRole('heading',{level:1}).waitFor()
  assert.equal(new URL(page.url()).pathname,'/')
  assert.equal(await page.locator('a[href="/compare"]').count(),0)
  assert.ok(!initialRequests.some(u=>/amap|CityMap|amapAdapter|CityStoryPage/.test(u)))
  await page.getByRole('link',{name:'开始风险学习',exact:false}).first().click()
  await page.waitForURL('**/login')
  check('public homepage, no map SDK, no fake business links, Learn protected')
  await page.goto(base+'/register')
  await page.getByLabel('用户名',{exact:true}).fill(username)
  await page.getByLabel('密码',{exact:true}).fill(password)
  await page.getByLabel('确认密码',{exact:true}).fill(password)
  assert.ok(username.length<=32)
  const registeredResponse=page.waitForResponse(r=>r.url().endsWith('/api/auth/register'))
  await page.getByRole('button',{name:'创 建 账 户'}).click()
  const registration=await registeredResponse
  assert.equal(registration.status(),200)
  evidence.userId=(await registration.json()).data.id
  await page.waitForURL('**/login')
  await page.locator('#username').fill(username)
  await page.locator('#password').fill(password)
  await page.getByRole('button',{name:'登录，继续学习'}).click()
  await page.waitForURL('**/dashboard')
  await page.getByRole('heading',{name:`欢迎回来，${username}`}).waitFor()
  const token=await page.evaluate(()=>sessionStorage.getItem('rentwise_access_token'))
  assert.ok(token && token.split('.').length===3)
  headers={Authorization:`Bearer ${token}`}
  check('real browser registration/login and authenticated Dashboard')

  for (const origin of [gateway, learning]) {
    const missing = await profileAt(origin, 404)
    assert.equal(missing.success, false)
    assert.equal(missing.data, null)
    assert.equal(missing.message, `Profile not initialized for user ${evidence.userId}`)
  }
  check('undiagnosed account: Gateway and Learning both 404 with explicit message, no profile data')
  // Use the JWT just returned by real login; no fabricated authentication.
  for (const routeName of ['/dashboard', '/results']) {
    await page.goto(base + routeName)
    await page.getByText('尚无学习画像', { exact: true }).waitFor()
    await page.getByText('请先完成初始诊断，再查看学习画像。', { exact: true }).waitFor()
    assert.equal(await page.getByText('学习画像加载失败', { exact: true }).count(), 0)
    await page.screenshot({ path: path.join(out, `real${routeName.replace('/', '-')}-missing.png`), fullPage: true })
    await page.getByRole('button', { name: '前往学习诊断', exact: true }).click()
    await page.waitForURL('**/diagnosis')
  }
  check('real 404 renders diagnosis guidance on Dashboard and Results; navigation works')

  const started=page.waitForResponse(r=>r.url().endsWith('/api/diagnosis/start'))
  await page.getByRole('button',{name:'开始学习诊断',exact:true}).click()
  const startResponse=await started;assert.equal(startResponse.status(),200)
  evidence.diagnosisSessionId=(await startResponse.json()).data.sessionId
  for(let i=0;i<10;i++){
    await page.getByRole('button',{name:'需要进一步澄清',exact:true}).click()
    const answered=page.waitForResponse(r=>/\/api\/diagnosis\/\d+\/answers$/.test(new URL(r.url()).pathname))
    await page.getByRole('button',{name:'提交答案',exact:true}).click()
    assert.equal((await answered).status(),200)
    if(i<9) await page.getByRole('button',{name:'下一题',exact:true}).click()
    else {
      const finished=page.waitForResponse(r=>/\/api\/diagnosis\/\d+\/finish$/.test(new URL(r.url()).pathname))
      await page.getByRole('button',{name:'完成诊断',exact:true}).click()
      assert.equal((await finished).status(),200)
    }
  }
  await page.getByText('学习诊断已完成',{exact:true}).waitFor()
  await page.screenshot({path:path.join(out,'real-diagnosis-completed.png'),fullPage:true})
  check('ten diagnosis answers and finish submitted through real browser UI')
  const gatewayProfile = await profileAt(gateway, 200)
  const directProfile = await profileAt(learning, 200)
  assert.deepEqual(gatewayProfile, directProfile)
  assert.equal(gatewayProfile.success, true)
  assert.equal(gatewayProfile.data.topics.length, 5)
  evidence.profileAfterDiagnosis = gatewayProfile.data
  check('same account after real diagnosis: both endpoints 200 with identical five-topic profile')
  await page.goto(base + '/dashboard')
  await page.getByRole('heading', { name: '知识主题掌握度', exact: true }).waitFor()
  await page.reload()
  await page.getByRole('heading', { name: '知识主题掌握度', exact: true }).waitFor()
  assert.equal(await page.getByText('尚无学习画像', { exact: true }).count(), 0)
  check('initialized Dashboard survives reload without empty state')

  const next = page.waitForResponse(res => res.url().endsWith('/api/training/next'))
  await page.goto(base + '/training')
  assert.equal((await next).status(), 200)
  await page.getByRole('button', { name: '需要进一步澄清', exact: true }).click()
  const training = page.waitForResponse(res => res.url().endsWith('/api/training/answers'))
  await page.getByRole('button', { name: '提交答案', exact: true }).click()
  const trainingResponse = await training
  assert.equal(trainingResponse.status(), 200)
  evidence.training = (await trainingResponse.json()).data
  assert(evidence.training.answer.answerId)
  assert(evidence.training.feedback.explanation)
  assert.equal(evidence.training.syncStatus, 'SYNCED')
  await page.screenshot({ path: path.join(out, 'real-training-feedback.png'), fullPage: true })
  check('real UI training answer persisted and feedback synchronized')

  await page.goto(base + '/assessment')
  await page.getByRole('button', { name: '开始五题测评', exact: true }).click()
  for (let i = 0; i < 5; i++) {
    await page.getByRole('button', { name: '需要进一步澄清', exact: true }).click()
    if (i < 4) await page.getByRole('button', { name: '下一题', exact: true }).click()
  }
  const assessment = page.waitForResponse(res => /\/api\/assessment\/\d+\/finish$/.test(new URL(res.url()).pathname))
  await page.getByRole('button', { name: '提交全部答案', exact: true }).click()
  const assessmentResponse = await assessment
  assert.equal(assessmentResponse.status(), 200)
  evidence.assessment = (await assessmentResponse.json()).data
  await page.getByText('阶段测评完成', { exact: true }).waitFor()
  await page.screenshot({ path: path.join(out, 'real-assessment.png'), fullPage: true })
  const finalProfile = await profileAt(gateway, 200)
  assert.deepEqual(finalProfile.data, evidence.assessment.profile)
  await page.goto(base + '/dashboard')
  await page.getByRole('heading', { name: '知识主题掌握度', exact: true }).waitFor()
  await page.screenshot({ path: path.join(out, 'real-dashboard-initialized.png'), fullPage: true })
  check('real assessment submitted; independent profile read matches and Dashboard reloads')
  await page.getByRole('button',{name:'退出登录',exact:true}).click()
  await page.waitForURL('**/login')
  assert.equal(await page.evaluate(()=>sessionStorage.getItem('rentwise_access_token')),null)
  await page.goto(base+'/dashboard');await page.waitForURL('**/login')
  await page.goto(base+'/')
  await page.getByRole('link',{name:'探索城市',exact:false}).first().click()
  await page.locator('.city-choice').first().waitFor()
  assert.equal(await page.locator('.city-choice').count(),8)
  for(const id of ['beijing','shanghai','suzhou']){
    await page.goto(base+'/')
    await page.locator(`a[href="/explore/stories/${id}"]`).click()
    await page.locator('.story-page h1').waitFor()
    await page.getByRole('navigation',{name:'故事导航'}).getByRole('link').click()
    await page.waitForURL('**/explore')
    await page.goBack();await page.waitForURL(`**/explore/stories/${id}`)
  }
  check('logout, protected redirect, homepage Explore eight cities, three stories and browser back')
  assert.deepEqual(evidence.pageErrors,[])
  evidence.status = 'passed'
} catch (error) {
  evidence.status = 'failed'
  evidence.failure = String(error)
  throw error
} finally {
  await writeFile(path.join(out, 'real-regression.json'), JSON.stringify(evidence, null, 2))
  await browser.close()
}
