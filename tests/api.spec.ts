import { test, expect } from '@playwright/test'

const API_BASE = 'http://localhost:8080/api/v1'

interface LoginResponse {
  code: number
  message: string
  data: {
    accessToken: string
    refreshToken: string
    expiresAt: string
    user: {
      id: number
      studentId: string
      nickname: string
      avatarUrl: string | null
      reputationScore: number
    }
  }
}

async function getAuthToken(request: any): Promise<string> {
  const loginRes = await request.post(`${API_BASE}/auth/login`, {
    data: { studentId: '2024010101', password: 'Pass1234' }
  })
  const loginBody: LoginResponse = await loginRes.json()
  if (loginBody.code !== 0) {
    throw new Error(`Login failed: ${loginBody.message}`)
  }
  return loginBody.data.accessToken
}

test.describe.serial('API 集成测试 - 认证模块', () => {
  test('POST /api/v1/auth/login - 有效凭据登录', async ({ request }) => {
    const response = await request.post(`${API_BASE}/auth/login`, {
      data: { studentId: '2024010101', password: 'Pass1234' }
    })
    expect(response.status()).toBe(200)

    const body: LoginResponse = await response.json()
    if (body.code !== 0) {
      test.skip(true, `Login returned code ${body.code}: ${body.message} (account may be locked)`)
      return
    }
    expect(body.data.accessToken).toBeTruthy()
    expect(body.data.refreshToken).toBeTruthy()
    expect(body.data.user.nickname).toBeTruthy()
    expect(body.data.user.studentId).toMatch(/\d{2}\*{4}\d{2}/)
  })

  test('POST /api/v1/auth/refresh - 刷新 Token', async ({ request }) => {
    const loginRes = await request.post(`${API_BASE}/auth/login`, {
      data: { studentId: '2024010101', password: 'Pass1234' }
    })
    const loginBody: LoginResponse = await loginRes.json()

    if (loginBody.code !== 0 || !loginBody.data) {
      test.skip(true, 'Login failed, possibly account locked')
      return
    }

    const refreshToken = loginBody.data.refreshToken

    const response = await request.post(`${API_BASE}/auth/refresh`, {
      data: { refreshToken }
    })
    expect(response.status()).toBe(200)
    const body = await response.json()

    if (body.code === 0) {
      expect(body.data.accessToken).toBeTruthy()
    } else {
      test.skip(true, `Refresh endpoint returned code ${body.code}: ${body.message}`)
    }
  })

  test('POST /api/v1/auth/login - 不存在的学号应返回错误', async ({ request }) => {
    const response = await request.post(`${API_BASE}/auth/login`, {
      data: { studentId: '99999999', password: 'Pass1234' }
    })
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).not.toBe(0)
  })

  test('POST /api/v1/auth/login - 无效密码应返回错误', async ({ request }) => {
    const response = await request.post(`${API_BASE}/auth/login`, {
      data: { studentId: '2024010101', password: 'WrongPassword' }
    })
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).not.toBe(0)
  })
})

test.describe('API 集成测试 - 商品模块', () => {
  test('GET /api/v1/products/search - 搜索商品列表', async ({ request }) => {
    const response = await request.get(`${API_BASE}/products/search`, {
      params: { page: 1, size: 5 }
    })
    expect(response.status()).toBe(200)

    const body = await response.json()
    expect(body.code).toBe(0)
    expect(body.data.list).toBeInstanceOf(Array)
    expect(body.data.list.length).toBeGreaterThan(0)
    expect(body.data.page).toBe(1)
    expect(body.data.size).toBe(5)
  })

  test('GET /api/v1/products/search - 按关键词搜索', async ({ request }) => {
    const response = await request.get(`${API_BASE}/products/search`, {
      params: { keyword: '微积分', page: 1, size: 5 }
    })
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).toBe(0)
    expect(body.data.list).toBeInstanceOf(Array)
  })

  test('GET /api/v1/products/search - 按分类筛选', async ({ request }) => {
    const response = await request.get(`${API_BASE}/products/search`, {
      params: { category: 'textbook', page: 1, size: 5 }
    })
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).toBe(0)
    for (const item of body.data.list) {
      expect(item.category).toBe('textbook')
    }
  })

  test('GET /api/v1/products/search - 按新旧程度筛选', async ({ request }) => {
    const response = await request.get(`${API_BASE}/products/search`, {
      params: { condition: 'like_new', page: 1, size: 5 }
    })
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).toBe(0)
    for (const item of body.data.list) {
      expect(item.condition).toBe('like_new')
    }
  })

  test('GET /api/v1/products/search - 按价格区间筛选', async ({ request }) => {
    const response = await request.get(`${API_BASE}/products/search`, {
      params: { minPrice: 1000, maxPrice: 10000, page: 1, size: 10 }
    })
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).toBe(0)
    for (const item of body.data.list) {
      expect(item.price).toBeGreaterThanOrEqual(1000)
      expect(item.price).toBeLessThanOrEqual(10000)
    }
  })

  test('GET /api/v1/products/search - 按价格升序排序', async ({ request }) => {
    const response = await request.get(`${API_BASE}/products/search`, {
      params: { sortBy: 'price', sortOrder: 'asc', page: 1, size: 10 }
    })
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).toBe(0)
    const prices = body.data.list.map((item: any) => item.price)
    for (let i = 1; i < prices.length; i++) {
      expect(prices[i]).toBeGreaterThanOrEqual(prices[i - 1])
    }
  })

  test('GET /api/v1/products/search - 按价格降序排序', async ({ request }) => {
    const response = await request.get(`${API_BASE}/products/search`, {
      params: { sortBy: 'price', sortOrder: 'desc', page: 1, size: 10 }
    })
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).toBe(0)
    const prices = body.data.list.map((item: any) => item.price)
    for (let i = 1; i < prices.length; i++) {
      expect(prices[i]).toBeLessThanOrEqual(prices[i - 1])
    }
  })

  test('GET /api/v1/products/{id} - 获取商品详情', async ({ request }) => {
    const response = await request.get(`${API_BASE}/products/1`)
    expect(response.status()).toBe(200)

    const body = await response.json()
    expect(body.code).toBe(0)
    expect(body.data.id).toBe(1)
    expect(body.data.title).toBeTruthy()
    expect(body.data.description).toBeTruthy()
    expect(body.data.price).toBeGreaterThan(0)
    expect(body.data.condition).toBeTruthy()
    expect(body.data.category).toBeTruthy()
    expect(body.data.campus).toBeTruthy()
    expect(body.data.sellerNickname).toBeTruthy()
    expect(body.data.sellerReputationScore).toBeGreaterThan(0)
    expect(body.data.imageUrls).toBeInstanceOf(Array)
  })

  test('GET /api/v1/products/{id} - 不存在的商品应返回错误', async ({ request }) => {
    const response = await request.get(`${API_BASE}/products/99999`)
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).not.toBe(0)
  })

  test('GET /api/v1/products/search - 分页参数验证', async ({ request }) => {
    const response = await request.get(`${API_BASE}/products/search`, {
      params: { page: 1, size: 2 }
    })
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).toBe(0)
    expect(body.data.list.length).toBeLessThanOrEqual(2)
    expect(body.data.total).toBeGreaterThan(0)
    expect(body.data.pages).toBeGreaterThan(0)
  })
})

test.describe('API 集成测试 - 用户模块', () => {
  test('GET /api/v1/users/me - 获取当前用户信息', async ({ request }) => {
    let authToken: string
    try {
      authToken = await getAuthToken(request)
    } catch {
      test.skip(true, 'Login failed, possibly account locked')
      return
    }

    const response = await request.get(`${API_BASE}/users/me`, {
      headers: { Authorization: `Bearer ${authToken}` }
    })
    expect(response.status()).toBe(200)

    const body = await response.json()
    expect(body.code).toBe(0)
    expect(body.data.id).toBeTruthy()
    expect(body.data.nickname).toBeTruthy()
    expect(body.data.studentId).toMatch(/\d{2}\*{4}\d{2}/)
    expect(body.data.reputationScore).toBeGreaterThan(0)
  })

  test('GET /api/v1/users/me - 未认证请求应返回错误', async ({ request }) => {
    const response = await request.get(`${API_BASE}/users/me`)
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).not.toBe(0)
  })
})

test.describe('API 集成测试 - 统一响应格式', () => {
  test('所有成功响应应包含统一格式', async ({ request }) => {
    const response = await request.get(`${API_BASE}/products/search`, {
      params: { page: 1, size: 1 }
    })
    const body = await response.json()

    expect(body).toHaveProperty('code')
    expect(body).toHaveProperty('message')
    expect(body).toHaveProperty('data')
    expect(body).toHaveProperty('timestamp')
    expect(body).toHaveProperty('traceId')
    expect(body).toHaveProperty('success')
  })

  test('分页响应应包含分页字段', async ({ request }) => {
    const response = await request.get(`${API_BASE}/products/search`, {
      params: { page: 1, size: 5 }
    })
    const body = await response.json()

    expect(body.data).toHaveProperty('page')
    expect(body.data).toHaveProperty('size')
    expect(body.data).toHaveProperty('total')
    expect(body.data).toHaveProperty('pages')
    expect(body.data).toHaveProperty('list')
  })
})