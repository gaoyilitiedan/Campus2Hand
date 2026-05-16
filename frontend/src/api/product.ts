import request from './request'

export interface ProductListParams {
  page?: number
  size?: number
  keyword?: string
  category?: string
  condition?: string
  campus?: string
  minPrice?: number
  maxPrice?: number
  sortBy?: string
  sortOrder?: string
}

export interface ProductItem {
  id: number
  title: string
  price: number
  originalPrice: number | null
  category: string
  condition: string
  campus: string
  status: string
  viewCount: number
  favoriteCount: number
  coverImage: string | null
  sellerNickname: string
  sellerReputationScore: number
  createdAt: string
}

export interface ProductDetail {
  id: number
  userId: number
  sellerNickname: string
  sellerAvatarUrl: string | null
  sellerReputationScore: number
  title: string
  description: string
  price: number
  originalPrice: number | null
  category: string
  condition: string
  campus: string
  status: string
  viewCount: number
  favoriteCount: number
  imageUrls: string[]
  isFavorited: boolean
  totalSold: number
  reviewCount: number
  createdAt: string
  updatedAt: string
}

export interface PageResponse<T> {
  page: number
  size: number
  total: number
  pages: number
  list: T[]
}

export function searchProducts(params: ProductListParams): Promise<PageResponse<ProductItem>> {
  return request.get('/products/search', { params }).then(res => res.data)
}

export function getProductDetail(id: number): Promise<ProductDetail> {
  return request.get(`/products/${id}`).then(res => res.data)
}

export function getMyProducts(page: number, size: number, status?: string): Promise<PageResponse<ProductItem>> {
  return request.get('/products/my', { params: { page, size, status } }).then(res => res.data)
}

export function publishProduct(data: any): Promise<ProductDetail> {
  return request.post('/products', data).then(res => res.data)
}

export function updateProduct(id: number, data: any): Promise<ProductDetail> {
  return request.put(`/products/${id}`, data).then(res => res.data)
}

export function toggleFavorite(id: number): Promise<void> {
  return request.post(`/products/${id}/favorite`).then(res => res.data)
}

export function getFavorites(page: number, size: number): Promise<PageResponse<ProductItem>> {
  return request.get('/products/favorites', { params: { page, size } }).then(res => res.data)
}

export function delistProduct(id: number): Promise<void> {
  return request.post(`/products/${id}/delist`).then(res => res.data)
}