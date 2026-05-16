<template>
  <div v-if="order" class="order-detail-page">
    <div class="page-header">
      <button class="btn btn-outline btn-sm" @click="$router.back()">← 返回</button>
      <h2>订单详情</h2>
    </div>

    <div class="detail-grid">
      <div class="card order-summary">
        <h3>订单概要</h3>
        <div class="summary-row"><span>订单编号</span><span>{{ order.orderNo }}</span></div>
        <div class="summary-row"><span>商品</span><span>{{ product?.title }}</span></div>
        <div class="summary-row"><span>金额</span><span class="price">{{ formatPrice(order.amount) }}</span></div>
        <div class="summary-row"><span>买家</span><span>{{ buyer?.nickname }}</span></div>
        <div class="summary-row"><span>卖家</span><span>{{ seller?.nickname }}</span></div>
        <div class="summary-row"><span>状态</span><StatusBadge :status="order.status" /></div>
      </div>

      <div class="card timeline-card">
        <h3>状态时间线</h3>
        <div class="timeline">
          <div class="tl-item done">
            <div class="tl-dot"></div>
            <div class="tl-content"><strong>订单创建</strong><span>{{ formatTime(order.createdAt) }}</span></div>
          </div>
          <div v-if="order.paidAt" class="tl-item done">
            <div class="tl-dot"></div>
            <div class="tl-content"><strong>已付款</strong><span>{{ formatTime(order.paidAt) }}</span></div>
          </div>
          <div v-if="order.status === 'refunding'" class="tl-item active">
            <div class="tl-dot"></div>
            <div class="tl-content"><strong>退款中</strong><span>卖家处理中</span></div>
          </div>
          <div v-if="order.status === 'refunded'" class="tl-item done">
            <div class="tl-dot"></div>
            <div class="tl-content"><strong>已退款</strong><span>{{ order.completedAt ? formatTime(order.completedAt) : '' }}</span></div>
          </div>
          <div v-if="order.status === 'completed'" class="tl-item done">
            <div class="tl-dot"></div>
            <div class="tl-content"><strong>已完成</strong><span>{{ order.completedAt ? formatTime(order.completedAt) : '' }}</span></div>
          </div>
          <div v-if="order.status === 'pending_payment'" class="tl-item pending">
            <div class="tl-dot"></div>
            <div class="tl-content"><strong>待付款</strong><span>等待买家付款</span></div>
          </div>
        </div>
      </div>
    </div>

    <div class="action-area card" v-if="showActions">
      <template v-if="order.status === 'pending_payment' && isBuyer">
        <button class="btn btn-primary btn-lg" @click="handlePay">💰 立即支付</button>
        <button class="btn btn-outline btn-lg" @click="cancelOrder">取消订单</button>
      </template>
      <template v-if="order.status === 'paid_escrow' && isBuyer">
        <button class="btn btn-success btn-lg" @click="showConfirmModal = true">✅ 确认收货</button>
        <button class="btn btn-outline btn-lg" @click="showRefundModal = true">↩️ 申请退款</button>
      </template>
      <template v-if="order.status === 'paid_escrow' && !isBuyer">
        <p class="waiting-text">⏳ 等待买家确认收货...</p>
      </template>
      <template v-if="order.status === 'completed'">
        <button class="btn btn-primary btn-lg" @click="$router.push('/my-reviews')">⭐ 去评价</button>
      </template>
      <template v-if="order.status === 'refunding'">
        <p class="waiting-text">🔄 退款处理中，请耐心等待...</p>
      </template>
    </div>

    <div v-if="showConfirmModal" class="modal-overlay" @click.self="showConfirmModal = false">
      <div class="modal-content">
        <h3 class="modal-title">确认收货</h3>
        <p>确认已收到商品？确认后资金将打款给卖家。</p>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showConfirmModal = false">取消</button>
          <button class="btn btn-success" @click="handleConfirm">确认收货</button>
        </div>
      </div>
    </div>

    <div v-if="showRefundModal" class="modal-overlay" @click.self="showRefundModal = false">
      <div class="modal-content">
        <h3 class="modal-title">申请退款</h3>
        <div class="form-group">
          <label>退款原因</label>
          <select v-model="refundReason">
            <option value="">请选择</option>
            <option value="not_as_described">商品与描述不符</option>
            <option value="seller_unreachable">卖家无法联系</option>
            <option value="product_damaged">商品有损坏</option>
            <option value="other">其他</option>
          </select>
        </div>
        <div class="form-group">
          <label>退款说明</label>
          <textarea v-model="refundDesc" rows="3" placeholder="请详细描述退款原因..."></textarea>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showRefundModal = false">取消</button>
          <button class="btn btn-danger" @click="handleRefund">提交退款申请</button>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="empty-state"><span class="icon">😕</span><p>订单不存在</p></div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { orders, getOrder, getProduct, getUser, formatPrice, currentUser } from '@/mock/data'
import StatusBadge from '@/components/StatusBadge.vue'

const route = useRoute()
const order = computed(() => getOrder(route.params.id as string))
const product = computed(() => order.value ? getProduct(order.value.productId) : null)
const buyer = computed(() => order.value ? getUser(order.value.buyerId) : null)
const seller = computed(() => order.value ? getUser(order.value.sellerId) : null)
const isBuyer = computed(() => order.value?.buyerId === currentUser.id)

const showConfirmModal = ref(false)
const showRefundModal = ref(false)
const refundReason = ref('')
const refundDesc = ref('')

const showActions = computed(() => {
  if (!order.value) return false
  return ['pending_payment', 'paid_escrow', 'completed', 'refunding'].includes(order.value.status)
})

function formatTime(t: string) { return new Date(t).toLocaleString('zh-CN') }

function handlePay() {
  if (!order.value) return
  order.value.status = 'paid_escrow'
  order.value.paidAt = new Date().toISOString()
  showToast('支付成功！')
}

function cancelOrder() {
  if (!order.value) return
  order.value.status = 'cancelled'
  showToast('订单已取消')
}

function handleConfirm() {
  if (!order.value) return
  order.value.status = 'completed'
  order.value.completedAt = new Date().toISOString()
  showConfirmModal.value = false
  showToast('确认收货成功！资金已结算给卖家')
}

function handleRefund() {
  if (!order.value || !refundReason.value) { alert('请选择退款原因'); return }
  order.value.status = 'refunding'
  order.value.refundReason = refundReason.value
  order.value.refundDesc = refundDesc.value
  showRefundModal.value = false
  showToast('退款申请已提交，等待卖家处理')
}

function showToast(msg: string) {
  const el = document.createElement('div'); el.className = 'toast'; el.textContent = msg
  document.body.appendChild(el); setTimeout(() => el.remove(), 3000)
}
</script>

<style scoped>
.page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
.page-header h2 { font-size: 20px; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px; }
.summary-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--border-light); font-size: 14px; }
.summary-row:last-child { border-bottom: none; }
.price { color: var(--danger); font-weight: 700; font-size: 16px; }
.timeline { padding: 8px 0; }
.tl-item { display: flex; gap: 12px; padding: 8px 0; position: relative; }
.tl-item:not(:last-child)::before { content: ''; position: absolute; left: 5px; top: 24px; bottom: -8px; width: 2px; background: var(--border); }
.tl-item.done .tl-dot { background: var(--success); }
.tl-item.active .tl-dot { background: var(--warning); animation: pulse 1.5s infinite; }
.tl-item.pending .tl-dot { background: var(--border); }
.tl-dot { width: 12px; height: 12px; border-radius: 50%; flex-shrink: 0; margin-top: 3px; }
@keyframes pulse { 0%, 100% { box-shadow: 0 0 0 0 rgba(250,173,20,0.4); } 50% { box-shadow: 0 0 0 8px rgba(250,173,20,0); } }
.tl-content { display: flex; flex-direction: column; gap: 2px; }
.tl-content strong { font-size: 14px; }
.tl-content span { font-size: 12px; color: var(--text-muted); }
.action-area { display: flex; gap: 12px; align-items: center; }
.waiting-text { font-size: 15px; color: var(--text-muted); }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; margin-bottom: 6px; font-weight: 500; font-size: 14px; }
.form-group select, .form-group textarea { width: 100%; padding: 10px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: 14px; }
.form-group textarea { resize: vertical; }
@media (max-width: 768px) { .detail-grid { grid-template-columns: 1fr; } }
</style>