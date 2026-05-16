import request from './request'

export interface OrderItem {
  id: number
  orderNo: string
  buyerId: number
  sellerId: number
  productId: number
  amount: number
  status: string
  paymentMethod: string | null
  paidAt: string | null
  completedAt: string | null
  cancelledAt: string | null
  expiresAt: string
  createdAt: string
  updatedAt: string
  productTitle?: string
  productCover?: string
  counterpartyName?: string
}

export function createOrder(productId: number): Promise<OrderItem> {
  return request.post('/orders', { productId }).then(res => res.data)
}

export function getMyOrders(page: number, size: number, role?: string, status?: string): Promise<any> {
  return request.get('/orders/my', { params: { page, size, role, status } }).then(res => res.data)
}

export function getOrderDetail(id: number): Promise<OrderItem> {
  return request.get(`/orders/${id}`).then(res => res.data)
}

export function payOrder(id: number): Promise<void> {
  return request.post(`/orders/${id}/pay`).then(res => res.data)
}

export function cancelOrder(id: number): Promise<void> {
  return request.post(`/orders/${id}/cancel`).then(res => res.data)
}

export function confirmReceipt(id: number): Promise<void> {
  return request.post(`/orders/${id}/confirm`).then(res => res.data)
}

export function requestRefund(id: number, reason: string, amount?: number): Promise<void> {
  return request.post(`/orders/${id}/refund`, { reason, amount }).then(res => res.data)
}