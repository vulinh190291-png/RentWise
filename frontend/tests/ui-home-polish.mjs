// UI 0.1.1 visual regression. No API writes or authentication mocking.
import { createRequire } from 'node:module'
import { mkdir, writeFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
const require = createRequire(import.meta.url)
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const browser = await chromium.launch({ channel: 'msedge', headless: true })
const page = await browser.newPage()
const base = process.env.UI_BASE_URL || 'http://127.0.0.1:5173'
const out = process.env.UI_SCREENSHOT_DIR
const prefix = process.env.UI_CAPTURE_PREFIX || 'polish'
const results = []; const failures = []; const errors = []
page.on('pageerror', error => errors.push(error.message))
if (out) await mkdir(out, { recursive: true })
try {
  for (const [width, height] of [[1440,900],[1024,768],[768,1024],[390,844],[320,740]]) {
    await page.setViewportSize({ width, height })
    await page.goto(base + '/login')
    await page.locator('#username').waitFor()
    await page.locator('.showcase > img').evaluate(img => img.decode())
    const form = await page.locator('.login-side').boundingBox()
    const image = await page.locator('.showcase').boundingBox()
    const submit = await page.locator('.login-button').boundingBox()
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth > innerWidth)
    results.push({ width,height,form,image,submit,overflow })
    if (overflow) failures.push(`horizontal overflow at ${width}`)
    if (width >= 1024 && form.y + form.height > height - 12) failures.push(`complete form below fold at ${width}: bottom ${form.y+form.height} > ${height-12}`)
    if (width >= 1024 && form.x <= image.x) failures.push(`not two columns at ${width}`)
    if (width < 768 && image.height > 340) failures.push(`image crowds mobile form at ${width}`)
    if (out) {
      await page.screenshot({path:`${out}/${prefix}-${width}.png`})
      if (width===390) await page.screenshot({path:`${out}/${prefix}-390-full.png`,fullPage:true})
    }
  }
  // Focus remains visible and the skip link takes keyboard users to the form.
  await page.goto(base+'/login'); await page.locator('.skip').waitFor(); await page.keyboard.press('Tab')
  assert.equal(await page.locator('.skip').evaluate(el=>el===document.activeElement),true)
  await page.keyboard.press('Enter'); await page.keyboard.press('Tab')
  assert.equal(await page.locator('#username').evaluate(el=>el===document.activeElement),true)
  await page.setViewportSize({width:1024,height:768})
  await page.goto(base+'/login'); await page.getByRole('button',{name:'登录，继续学习'}).click()
  await page.getByText('请输入用户名',{exact:true}).waitFor()
  const errorSubmit = await page.locator('.login-button').boundingBox()
  if (errorSubmit.y+errorSubmit.height>768) failures.push('validation pushes submit below desktop fold')
  await page.setViewportSize({width:1440,height:900})
  await page.goto(base+'/login'); await page.locator('.showcase > img').waitFor()
  for (const title of ['普通公寓','复式空间','院落住宅']) {
    await page.getByRole('button',{name:`查看${title}`,exact:true}).click()
    await page.locator('.showcase > img').evaluate(img=>img.decode())
    assert(await page.locator('.showcase > img').evaluate(img=>img.naturalWidth>0))
    if(out) await page.screenshot({path:`${out}/${prefix}-story-${title}.png`})
  }
  await page.setViewportSize({width:390,height:844})
  await page.goto(base+'/login'); await page.locator('.nav-login').click()
  assert((await page.locator('#username').boundingBox()).y < 844)
  if(out) await page.screenshot({path:`${out}/${prefix}-390-login.png`})
  assert.deepEqual(errors,[])
  assert.deepEqual(failures,[])
  console.log('PASS: 5 viewports, full desktop form, compact mobile image, skip-link focus, visible validation, no runtime errors')
} finally {
  if(out) await writeFile(`${out}/${prefix}-measurements.json`,JSON.stringify({results,failures,errors},null,2))
  await browser.close()
}
