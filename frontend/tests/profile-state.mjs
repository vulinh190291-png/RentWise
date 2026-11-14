// Browser contract regression with explicit fixtures, NOT real backend acceptance.
// Set PLAYWRIGHT_MODULE to an existing Playwright directory (no dependency changes).
import assert from 'node:assert/strict'
import { pathToFileURL } from 'node:url'
import path from 'node:path'
import { mkdir } from 'node:fs/promises'

if (!process.env.PLAYWRIGHT_MODULE) throw new Error('Set PLAYWRIGHT_MODULE to an existing Playwright directory')
const { chromium } = await import(pathToFileURL(path.join(process.env.PLAYWRIGHT_MODULE, 'index.mjs')).href)
const browser = await chromium.launch({ channel: 'msedge', headless: true })
if (process.env.EVIDENCE_DIR) await mkdir(process.env.EVIDENCE_DIR, { recursive: true })
const topics = ['DEPOSIT_RETURN', 'EARLY_TERMINATION', 'REPAIR_RESPONSIBILITY', 'COST_BEARING', 'BREACH_LIABILITY']
const profile = { userId: 42, topics: topics.map(topic => ({ topic, topicName: topic, masteryScore: 60, level: 'NORMAL', consecutiveWrong: 0, lastDifficulty: 'EASY' })) }
let passed = 0
try {
  for (const routeName of ['/dashboard', '/results']) {
    for (const state of ['missing', 'initialized', 'unavailable', 'network']) {
      const page = await browser.newPage()
      page.setDefaultTimeout(5000)
      const pageErrors = []
      page.on('pageerror', error => pageErrors.push(error.message))
      let recommendationCalls = 0
      await page.addInitScript(() => sessionStorage.setItem('rentwise_access_token', 'contract-test-only'))
      await page.route(url => url.pathname.startsWith('/api/'), async route => {
        const url = new URL(route.request().url())
        const reply = (status, data, message = 'OK') => route.fulfill({ status, json: { success: status === 200, data, message } })
        if (url.pathname === '/api/users/me') return reply(200, { id: 42, username: 'contract-test', role: 'LEARNER' })
        if (url.pathname === '/api/profile/me') {
          if (state === 'network') return route.abort('connectionrefused')
          if (state === 'initialized') return reply(200, profile)
          return reply(state === 'missing' ? 404 : 502, null, state === 'missing' ? 'Profile not initialized for user 42' : 'Profile Service request failed: ServiceUnavailable')
        }
        if (url.pathname === '/api/training/next') {
          recommendationCalls++
          return reply(200, { userId: 42, topic: 'DEPOSIT_RETURN', difficulty: 'EASY', reason: 'contract fixture', learningCard: null, trainingCase: { caseId: 1, topic: 'DEPOSIT_RETURN', difficulty: 'EASY', clauseText: 'contract fixture clause', question: 'contract fixture question' } })
        }
        throw new Error(`Unexpected API request: ${url.pathname}`)
      })
      await page.goto(`${process.env.FRONTEND_URL || 'http://localhost:5173'}${routeName}`)
      if (state === 'missing') {
        await page.getByText('尚无学习画像', { exact: true }).waitFor()
        await page.getByText('请先完成初始诊断，再查看学习画像。', { exact: true }).waitFor()
        assert.equal(await page.getByText('Request failed with status code 404').count(), 0)
        if (process.env.EVIDENCE_DIR) await page.screenshot({ path: path.join(process.env.EVIDENCE_DIR, `contract${routeName.replace('/', '-')}-missing.png`), fullPage: true })
        await page.getByRole('button', { name: '前往学习诊断', exact: true }).click()
        await page.waitForURL('**/diagnosis')
        assert.equal(recommendationCalls, 0)
      } else if (state === 'initialized') {
        await page.locator('p').filter({ hasText: 'contract fixture' }).first().waitFor()
        assert.equal(await page.getByText('尚无学习画像', { exact: true }).count(), 0)
        assert.equal(recommendationCalls, 1)
      } else {
        await page.getByText('学习画像加载失败', { exact: true }).waitFor()
        assert.equal(await page.getByText('尚无学习画像', { exact: true }).count(), 0)
        assert.equal(recommendationCalls, 0)
        await page.getByRole('button', { name: '重新加载', exact: true }).waitFor()
      }
      assert.deepEqual(pageErrors, [])
      await page.close()
      console.log(`PASS ${routeName}: ${state}`)
      passed++
    }
  }
} finally {
  await browser.close()
}
console.log(`${passed} browser contract checks passed (mocked HTTP, not real integration)`)
