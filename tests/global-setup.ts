import { chromium, FullConfig } from '@playwright/test'

async function globalSetup(config: FullConfig) {
  const { baseURL } = config.projects[0].use
  const browser = await chromium.launch({ headless: true })
  const page = await browser.newPage()

  await page.goto(`${baseURL}/login`)
  await page.fill('input[placeholder="请输入学号"]', '2024010101')
  await page.fill('input[placeholder="请输入密码"]', 'Pass1234')
  await page.click('button[type="submit"]')
  await page.waitForURL('**/')
  await page.waitForSelector('.user-name')

  await page.context().storageState({ path: 'tests/.auth/user.json' })
  await browser.close()
}

export default globalSetup