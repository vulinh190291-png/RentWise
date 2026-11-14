// Explicit opt-in: creates ONE dedicated account and real diagnosis/training/assessment records.
// No mocked responses, DB access, secret files, or cleanup of any user data.
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
const username = `profile_fix_${Date.now()}`
const password = `Test!${randomBytes(18).toString('hex')}`
const evidence = { time: new Date().toISOString(), mode: 'REAL HTTP, no mocks', username, checks: [], requests: [] }
const check = name => { evidence.checks.push(name); console.log('PASS', name) }
let headers = {}
async function post(endpoint, data) {
  const res = await page.request.post(gateway + endpoint, { headers, data })
  evidence.requests.push({ path: endpoint, status: res.status() })
  assert.equal(res.status(), 200, endpoint)
  const body = await res.json()
  assert.equal(body.success, true)
  return body.data
}
async function profileAt(origin, expectedStatus) {
  const res = await page.request.get(origin + '/api/profile/me', { headers })
  const body = await res.json()
  evidence.requests.push({ path: origin + '/api/profile/me', status: res.status(), body })
  assert.equal(res.status(), expectedStatus)
  return body
}
try {
  const registered = await post('/api/auth/register', { username, password })
  evidence.userId = registered.id
  const login = await post('/api/auth/login', { username, password })
  headers = { Authorization: `Bearer ${login.accessToken}` }
  const me = await page.request.get(gateway + '/api/users/me', { headers })
  assert.equal(me.status(), 200)
  assert.equal((await me.json()).data.id, evidence.userId)
  check('real registration, login, and authenticated users/me')

  for (const origin of [gateway, learning]) {
    const missing = await profileAt(origin, 404)
    assert.equal(missing.success, false)
    assert.equal(missing.data, null)
    assert.equal(missing.message, `Profile not initialized for user ${evidence.userId}`)
  }
  check('undiagnosed account: Gateway and Learning both 404 with explicit message, no profile data')
  // Use the JWT just returned by real login; no fabricated authentication.
  await page.addInitScript(token => sessionStorage.setItem('rentwise_access_token', token), login.accessToken)
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

  const diagnosis = await post('/api/diagnosis/start')
  evidence.diagnosisSessionId = diagnosis.sessionId
  assert.equal(diagnosis.cases.length, 10)
  for (const item of diagnosis.cases) {
    await post(`/api/diagnosis/${diagnosis.sessionId}/answers`, { caseId: item.caseId, selectedClarify: true })
  }
  await post(`/api/diagnosis/${diagnosis.sessionId}/finish`)
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
  evidence.status = 'passed'
} catch (error) {
  evidence.status = 'failed'
  evidence.failure = String(error)
  throw error
} finally {
  await writeFile(path.join(out, 'real-regression.json'), JSON.stringify(evidence, null, 2))
  await browser.close()
}
