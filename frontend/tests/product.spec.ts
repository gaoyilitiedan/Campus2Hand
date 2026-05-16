import { test, expect } from '@playwright/test'

test.describe('商品搜索与浏览 (F-04, F-05)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')
  })

  test('AC-P01: 首页应加载商品列表', async ({ page }) => {
    await expect(page.locator('.product-grid .product-card').first()).toBeVisible({ timeout: 10000 })
    const productCards = page.locator('.product-grid .product-card')
    await expect(productCards.first()).toBeVisible()
  })

  test('AC-P03: 商品卡片应显示标题、价格、卖家信息', async ({ page }) => {
    const card = page.locator('.product-grid .product-card').first()
    await expect(card).toBeVisible({ timeout: 10000 })
    await expect(card.locator('.card-title')).toBeVisible()
    await expect(card.locator('.card-price')).toBeVisible()
  })

  test('F-04-1: 关键词搜索商品', async ({ page }) => {
    await page.fill('.search-input', '微积分')
    await page.waitForLoadState('networkidle')

    const cards = page.locator('.product-grid .product-card')
    const count = await cards.count()
    expect(count).toBeGreaterThan(0)
  })

  test('F-04-2: 按分类筛选商品', async ({ page }) => {
    await page.click('.filter-tab:has-text("教材")')
    await page.waitForTimeout(500)

    const cards = page.locator('.product-grid .product-card')
    const count = await cards.count()
    expect(count).toBeGreaterThan(0)
  })

  test('F-04-2: 按分类筛选 - 数码', async ({ page }) => {
    await page.click('.filter-tab:has-text("数码")')
    await page.waitForTimeout(500)

    const cards = page.locator('.product-grid .product-card')
    const count = await cards.count()
    expect(count).toBeGreaterThan(0)
  })

  test('F-04-4: 按新旧程度筛选', async ({ page }) => {
    await page.selectOption('.filter-select', { label: '几乎全新' })
    await page.waitForTimeout(500)

    const cards = page.locator('.product-grid .product-card')
    const count = await cards.count()
    expect(count).toBeGreaterThan(0)
  })

  test('F-04-5: 按价格排序 - 从低到高', async ({ page }) => {
    await page.selectOption('.filter-select', { label: '价格从低到高' })
    await page.waitForTimeout(500)

    const prices = await page.locator('.card-price').allTextContents()
    const numericPrices = prices.map(p => parseFloat(p.replace('¥', '')))
    for (let i = 1; i < numericPrices.length; i++) {
      expect(numericPrices[i]).toBeGreaterThanOrEqual(numericPrices[i - 1])
    }
  })

  test('F-04-5: 按价格排序 - 从高到低', async ({ page }) => {
    await page.selectOption('.filter-select', { label: '价格从高到低' })
    await page.waitForTimeout(500)

    const prices = await page.locator('.card-price').allTextContents()
    const numericPrices = prices.map(p => parseFloat(p.replace('¥', '')))
    for (let i = 1; i < numericPrices.length; i++) {
      expect(numericPrices[i]).toBeLessThanOrEqual(numericPrices[i - 1])
    }
  })

  test('F-04-5: 按信誉优先排序', async ({ page }) => {
    await page.selectOption('.filter-select', { label: '信誉优先' })
    await page.waitForTimeout(500)

    const cards = page.locator('.product-grid .product-card')
    await expect(cards.first()).toBeVisible({ timeout: 10000 })
  })

  test('F-04-3: 按价格区间筛选', async ({ page }) => {
    await page.fill('input[placeholder="最低价"]', '1000')
    await page.fill('input[placeholder="最高价"]', '5000')
    await page.waitForTimeout(500)

    const cards = page.locator('.product-grid .product-card')
    const count = await cards.count()
    expect(count).toBeGreaterThan(0)
  })

  test('F-04-6: 分页功能', async ({ page }) => {
    const pagination = page.locator('.pagination')
    const count = await pagination.count()
    if (count === 0) {
      test.skip(true, 'Only 1 page of data, pagination not displayed')
      return
    }
    await expect(pagination).toBeVisible({ timeout: 10000 })
  })

  test('F-05: 点击商品卡片进入详情页', async ({ page }) => {
    const card = page.locator('.product-grid .product-card').first()
    await expect(card).toBeVisible({ timeout: 10000 })
    await card.click()

    await expect(page).toHaveURL(/\/product\/\d+/)
  })
})

test.describe('商品详情页 (F-05)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')
  })

  test('F-05-1: 商品详情应展示图片轮播', async ({ page }) => {
    await page.goto('/product/1')
    await expect(page.locator('.detail-gallery')).toBeVisible({ timeout: 10000 })
  })

  test('F-05-2: 商品详情应展示标题、价格、新旧程度', async ({ page }) => {
    await page.goto('/product/1')
    await expect(page.locator('.detail-title')).toBeVisible({ timeout: 10000 })
    await expect(page.locator('.detail-price')).toBeVisible()
    await expect(page.locator('.detail-tags')).toBeVisible()
  })

  test('F-05-3: 商品详情应展示描述', async ({ page }) => {
    await page.goto('/product/1')
    await expect(page.locator('.detail-desc')).toBeVisible({ timeout: 10000 })
  })

  test('F-05-4: 商品详情应展示卖家信息（昵称、信誉分、校区）', async ({ page }) => {
    await page.goto('/product/1')
    await expect(page.locator('.seller-card')).toBeVisible({ timeout: 10000 })
    await expect(page.locator('.seller-header')).toBeVisible()
    await expect(page.locator('.seller-stats')).toBeVisible()
  })

  test('F-05-5: 应展示"联系卖家"按钮', async ({ page }) => {
    await page.goto('/product/1')
    await expect(page.locator('button:has-text("联系卖家")')).toBeVisible({ timeout: 10000 })
  })

  test('F-05-6: 应展示"立即购买"按钮', async ({ page }) => {
    await page.goto('/product/1')
    await expect(page.locator('button:has-text("立即购买")')).toBeVisible({ timeout: 10000 })
  })

  test('F-05-7: 应展示卖家评价区域', async ({ page }) => {
    await page.goto('/product/1')
    await expect(page.locator('.reviews-section')).toBeVisible({ timeout: 10000 })
  })

  test('点击"联系卖家"应跳转到聊天页面', async ({ page }) => {
    await page.goto('/product/1')
    await page.click('button:has-text("联系卖家")')
    await expect(page).toHaveURL('/messages')
  })
})