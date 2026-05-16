import { test, expect } from '@playwright/test'

test.describe('个人中心 (F-15)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')
  })

  test('个人中心应展示用户昵称', async ({ page }) => {
    await page.goto('/profile')
    await expect(page.locator('.profile-header h2')).toBeVisible({ timeout: 10000 })
  })

  test('个人中心应展示信誉分', async ({ page }) => {
    await page.goto('/profile')
    await expect(page.locator('.profile-score')).toBeVisible({ timeout: 10000 })
  })

  test('个人中心应展示校区信息', async ({ page }) => {
    await page.goto('/profile')
    await expect(page.locator('.profile-campus')).toBeVisible({ timeout: 10000 })
  })

  test('个人中心应展示统计数据（发布数、成交数、评价数）', async ({ page }) => {
    await page.goto('/profile')
    await expect(page.locator('.stats-row')).toBeVisible({ timeout: 10000 })
    const statItems = page.locator('.stat-item')
    await expect(statItems).toHaveCount(3)
  })

  test('F-15-1: 学号应脱敏展示', async ({ page }) => {
    await page.goto('/profile')
    const studentIdText = await page.locator('.info-item .info-value').first().textContent()
    expect(studentIdText).toMatch(/\d{2}\*{4}\d{2}/)
  })

  test('F-15-2: 个人信息应展示校园邮箱', async ({ page }) => {
    await page.goto('/profile')
    await expect(page.locator('.info-card')).toBeVisible({ timeout: 10000 })
  })

  test('个人中心应有退出登录按钮', async ({ page }) => {
    await page.goto('/profile')
    await expect(page.locator('button:has-text("退出登录")')).toBeVisible({ timeout: 10000 })
  })

  test('个人中心应有修改密码按钮', async ({ page }) => {
    await page.goto('/profile')
    await expect(page.locator('button:has-text("修改密码")')).toBeVisible({ timeout: 10000 })
  })

  test('个人中心应有切换校区按钮', async ({ page }) => {
    await page.goto('/profile')
    await expect(page.locator('button:has-text("切换校区")')).toBeVisible({ timeout: 10000 })
  })

  test('点击切换校区应弹出校区选择弹窗', async ({ page }) => {
    await page.goto('/profile')
    await page.click('button:has-text("切换校区")')
    await expect(page.locator('.modal-content')).toBeVisible({ timeout: 5000 })
  })
})

test.describe('侧边栏导航', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')
  })

  const navItems = [
    { path: '/', label: '商品市场' },
    { path: '/publish', label: '发布商品' },
    { path: '/my-products', label: '我的商品' },
    { path: '/my-orders', label: '我的订单' },
    { path: '/messages', label: '聊天消息' },
    { path: '/my-reviews', label: '评价管理' },
    { path: '/my-disputes', label: '投诉工单' },
    { path: '/profile', label: '个人中心' }
  ]

  for (const item of navItems) {
    test(`侧边栏导航到"${item.label}"`, async ({ page }) => {
      await page.click(`.nav-item:has-text("${item.label}")`)
      if (item.path === '/') {
        await expect(page).toHaveURL('/')
      } else {
        await expect(page).toHaveURL(item.path)
      }
    })
  }
})

test.describe('Header 组件', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')
  })

  test('Header 应展示平台名称', async ({ page }) => {
    await expect(page.locator('.header-title')).toContainText('Campus2Hand')
  })

  test('Header 应展示用户昵称', async ({ page }) => {
    await expect(page.locator('.user-name')).toBeVisible()
  })

  test('点击用户下拉菜单应展示选项', async ({ page }) => {
    await page.click('.user-dropdown')
    await expect(page.locator('.dropdown-menu')).toBeVisible()
    await expect(page.locator('.dropdown-item:has-text("个人中心")')).toBeVisible()
    await expect(page.locator('.dropdown-item:has-text("我的订单")')).toBeVisible()
    await expect(page.locator('.dropdown-item:has-text("退出登录")')).toBeVisible()
  })
})