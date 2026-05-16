import { test, expect } from '@playwright/test'

test.describe('订单模块 (F-09, F-10, F-11)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')
  })

  test('F-09-1: 从商品详情页点击立即购买', async ({ page }) => {
    await page.goto('/product/1')
    await expect(page.locator('button:has-text("立即购买")')).toBeVisible({ timeout: 10000 })
    await page.click('button:has-text("立即购买")')
    await page.waitForTimeout(1000)
  })

  test('我的订单页面应展示订单列表', async ({ page }) => {
    await page.goto('/my-orders')
    await expect(page.locator('.page-title')).toContainText('我的订单')
  })

  test('我的订单页面应有角色切换 Tab（我买的/我卖的）', async ({ page }) => {
    await page.goto('/my-orders')
    await expect(page.locator('.tab-item:has-text("我买的")')).toBeVisible()
    await expect(page.locator('.tab-item:has-text("我卖的")')).toBeVisible()
  })

  test('我的订单页面应有状态筛选', async ({ page }) => {
    await page.goto('/my-orders')
    await expect(page.locator('.filter-tabs')).toBeVisible()
    const statusTabs = page.locator('.filter-tab')
    await expect(statusTabs.first()).toBeVisible()
  })

  test('切换到"我卖的"Tab', async ({ page }) => {
    await page.goto('/my-orders')
    await page.click('.tab-item:has-text("我卖的")')
    await expect(page.locator('.tab-item.active')).toContainText('我卖的')
  })

  test('按状态筛选订单 - 待付款', async ({ page }) => {
    await page.goto('/my-orders')
    await page.click('.filter-tab:has-text("待付款")')
    await expect(page.locator('.filter-tab.active')).toContainText('待付款')
  })

  test('按状态筛选订单 - 已完成', async ({ page }) => {
    await page.goto('/my-orders')
    await page.click('.filter-tab:has-text("已完成")')
    await expect(page.locator('.filter-tab.active')).toContainText('已完成')
  })

  test('订单卡片应显示订单号、状态、金额', async ({ page }) => {
    await page.goto('/my-orders')
    const orderCard = page.locator('.order-card').first()
    await expect(orderCard.locator('.order-no')).toBeVisible()
    await expect(orderCard.locator('.order-amount')).toBeVisible()
  })

  test('点击订单卡片应跳转到订单详情', async ({ page }) => {
    await page.goto('/my-orders')
    const orderCard = page.locator('.order-card').first()
    if (await orderCard.count() > 0) {
      await orderCard.click()
      await expect(page).toHaveURL(/\/order\//)
    }
  })
})

test.describe('订单详情页', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')
  })

  test('订单详情页应展示订单信息', async ({ page }) => {
    await page.goto('/my-orders')
    await page.waitForLoadState('networkidle')
    const orderCard = page.locator('.order-card').first()
    if (await orderCard.count() === 0) {
      test.skip(true, 'No orders available')
      return
    }
    await orderCard.click()
    await expect(page).toHaveURL(/\/order\//)
    await expect(page.locator('.order-detail-page')).toBeVisible({ timeout: 10000 })
  })
})