import { test, expect } from '@playwright/test'

test.describe('商品发布 (F-06)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')
  })

  test('F-06: 发布页面应包含所有必填字段', async ({ page }) => {
    await page.goto('/publish')
    await expect(page.locator('.page-title')).toContainText('发布闲置商品')

    await expect(page.locator('input[placeholder="请输入商品标题"]')).toBeVisible()
    await expect(page.locator('textarea[placeholder*="请详细描述"]')).toBeVisible()
    await expect(page.locator('input[placeholder="0.00"]')).toBeVisible()
    await expect(page.locator('select')).toHaveCount(3)
  })

  test('F-06-3: 标题长度限制 60 字符', async ({ page }) => {
    await page.goto('/publish')
    const titleInput = page.locator('input[placeholder="请输入商品标题"]')
    await titleInput.fill('这是一个超过六十个字符的标题测试这是一个超过六十个字符的标题测试这是一个超过六十个字符的标题测试123')
    const value = await titleInput.inputValue()
    expect(value.length).toBeLessThanOrEqual(60)
  })

  test('F-06-4: 描述长度限制 2000 字符', async ({ page }) => {
    await page.goto('/publish')
    const descInput = page.locator('textarea[placeholder*="请详细描述"]')
    await expect(descInput).toHaveAttribute('maxlength', '2000')
  })

  test('F-06-5: 价格输入框应存在', async ({ page }) => {
    await page.goto('/publish')
    await expect(page.locator('input[placeholder="0.00"]')).toBeVisible()
  })

  test('F-06-6: 分类下拉框应包含所有选项', async ({ page }) => {
    await page.goto('/publish')
    const categorySelect = page.locator('select').first()
    const options = await categorySelect.locator('option').allTextContents()
    expect(options).toContain('教材')
    expect(options).toContain('数码')
    expect(options).toContain('生活用品')
    expect(options).toContain('运动器材')
    expect(options).toContain('服饰')
    expect(options).toContain('其他')
  })

  test('F-06-7: 新旧程度单选按钮应存在', async ({ page }) => {
    await page.goto('/publish')
    await expect(page.locator('.radio-group')).toBeVisible()
    const radioItems = page.locator('.radio-item')
    await expect(radioItems).toHaveCount(4)
  })

  test('F-06-8: 校区下拉框应包含校区选项', async ({ page }) => {
    await page.goto('/publish')
    const campusSelect = page.locator('select').nth(1)
    const options = await campusSelect.locator('option').allTextContents()
    expect(options.some(o => o.includes('校区'))).toBeTruthy()
  })

  test('F-06-1: 图片上传区域应存在', async ({ page }) => {
    await page.goto('/publish')
    await expect(page.locator('.upload-grid')).toBeVisible()
    await expect(page.locator('.upload-add')).toBeVisible()
  })

  test('F-06-1: 点击 + 可添加模拟图片', async ({ page }) => {
    await page.goto('/publish')
    await page.click('.upload-add')
    await expect(page.locator('.upload-item')).toHaveCount(1)
  })

  test('F-06-9: 填写完整表单并提交', async ({ page }) => {
    await page.goto('/publish')

    await page.click('.upload-add')
    await page.fill('input[placeholder="请输入商品标题"]', '测试商品 - Playwright E2E')
    await page.fill('textarea[placeholder*="请详细描述"]', '这是一个由自动化测试创建的商品描述。')
    await page.fill('input[placeholder="0.00"]', '99.99')
    await page.selectOption('select', { label: '数码' })
    await page.locator('.radio-item:has-text("几乎全新")').click()

    await page.click('button:has-text("发布商品")')

    await expect(page.locator('.success-card')).toBeVisible({ timeout: 5000 })
    await expect(page.locator('.success-card h2')).toContainText('发布成功')
  })

  test('F-06-9: 发布成功后点击查看我的商品', async ({ page }) => {
    await page.goto('/publish')
    await page.click('.upload-add')
    await page.fill('input[placeholder="请输入商品标题"]', 'E2E 测试商品')
    await page.fill('textarea[placeholder*="请详细描述"]', '自动化测试描述')
    await page.fill('input[placeholder="0.00"]', '50')
    await page.selectOption('select', { label: '教材' })
    await page.locator('.radio-item:has-text("几乎全新")').click()
    await page.click('button:has-text("发布商品")')

    await expect(page.locator('.success-card')).toBeVisible({ timeout: 5000 })
    await page.click('button:has-text("查看我的商品")')
    await expect(page).toHaveURL('/my-products')
  })
})

test.describe('商品管理 (F-07)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')
  })

  test('F-07-1: 我的商品页面应展示商品列表', async ({ page }) => {
    await page.goto('/my-products')
    await expect(page.locator('h2')).toContainText('我的商品')
  })

  test('F-07-1: 我的商品页面应有状态筛选 Tab', async ({ page }) => {
    await page.goto('/my-products')
    await expect(page.locator('.tabs')).toBeVisible()
  })
})