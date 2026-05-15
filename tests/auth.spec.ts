import { test, expect } from '@playwright/test'

const TEST_USER = {
  studentId: '2024010101',
  password: 'Pass1234',
  nickname: '张三同学'
}

test.describe.serial('认证模块 (F-01, F-02, F-03)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
  })

  test('AC-A04: 使用有效凭据登录', async ({ page }) => {
    await page.fill('input[placeholder="请输入学号"]', TEST_USER.studentId)
    await page.fill('input[placeholder="请输入密码"]', TEST_USER.password)
    await page.click('button[type="submit"]')

    await expect(page).toHaveURL('/')
    await expect(page.locator('.user-name')).toContainText(TEST_USER.nickname)
  })

  test('登录页面应显示测试账号提示', async ({ page }) => {
    await expect(page.locator('.login-footer')).toContainText('2024010101')
  })

  test('登录页面应显示平台名称', async ({ page }) => {
    await expect(page.locator('.login-header h1')).toContainText('Campus2Hand')
  })

  test('AC-A04: 使用无效密码登录应显示错误', async ({ page }) => {
    await page.fill('input[placeholder="请输入学号"]', TEST_USER.studentId)
    await page.fill('input[placeholder="请输入密码"]', 'WrongPassword')
    await page.click('button[type="submit"]')

    await expect(page.locator('.error-msg')).toBeVisible()
  })

  test('AC-A04: 使用无效学号登录应显示错误', async ({ page }) => {
    await page.fill('input[placeholder="请输入学号"]', '99999999')
    await page.fill('input[placeholder="请输入密码"]', TEST_USER.password)
    await page.click('button[type="submit"]')

    await expect(page.locator('.error-msg')).toBeVisible()
  })
})

test.describe('路由守卫', () => {
  test('未登录用户访问受保护页面应重定向到登录页', async ({ page }) => {
    await page.goto('/profile')
    await expect(page).toHaveURL('/login')
  })

  test('未登录用户访问发布商品页应重定向到登录页', async ({ page }) => {
    await page.goto('/publish')
    await expect(page).toHaveURL('/login')
  })

  test('已登录用户访问登录页应重定向到首页', async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="请输入学号"]', TEST_USER.studentId)
    await page.fill('input[placeholder="请输入密码"]', TEST_USER.password)
    await page.click('button[type="submit"]')
    await expect(page).toHaveURL('/')

    await page.goto('/login')
    await expect(page).toHaveURL('/')
  })
})

test.describe('退出登录', () => {
  test('退出登录后应跳转到登录页', async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="请输入学号"]', TEST_USER.studentId)
    await page.fill('input[placeholder="请输入密码"]', TEST_USER.password)
    await page.click('button[type="submit"]')
    await expect(page).toHaveURL('/')

    await page.click('.user-dropdown')
    await page.waitForSelector('.dropdown-menu', { state: 'visible' })
    await page.click('.dropdown-item.logout')

    await expect(page).toHaveURL('/login')
  })
})