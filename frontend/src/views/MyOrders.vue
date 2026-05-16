<template>
  <div class="orders-page">
    <h2 class="page-title">🛒 我的订单</h2>
    <div class="tabs">
      <span class="tab-item" :class="{ active: role === 'buyer' }" @click="role = 'buyer'">我买的</span>
      <span class="tab-item" :class="{ active: role === 'seller' }" @click="role = 'seller'">我卖的</span>
    </div>
    <div class="filter-tabs">
      <span v-for="s in statuses" :key="s.value" class="filter-tab" :class="{ active: statusFilter === s.value }" @click="statusFilter = s.value">{{ s.label }}</span>
    </div>

    <div v-if="filteredOrders.length" class="order-list">
      <div v-for="o in filteredOrders" :key="o.id" class="order-card card" @click="$router.push(`/order/${o.id}`)">
        <div class="order-header">
          <span class="order-no">订单号: {{ o.orderNo }}</span>
          <StatusBadge :status="o.status" />
        </div>
        <div class="order-body">
          <div class="thumb" :style="{ backgroundColor: getProduct(o.productId)?.images?.[0] || '#e0e0e0' }"></div>
          <div class="order-info">
            <h4>{{ getProduct(o.productId)?.title }}</h4>
            <p class="order-counterparty">{{ role === 'buyer' ? '卖家' : '买家' }}: {{ counterpartyName(o) }}</p>
            <p class="order-time">{{ relativeTime(o.createdAt) }}</p>
          </div>
          <div class="order-amount">{{ formatPrice(o.amount) }}</div>
        </div>
        <div class="order-actions" @click.stop>
          <button v-if="o.status === 'pending_payment' && role === 'buyer'" class="btn btn-primary btn-sm" @click="payOrder(o)">去支付</button>
          <button v-if="o.status === 'paid_escrow' && role === 'buyer'" class="btn btn-success btn-sm" @click="confirmReceipt(o)">确认收货</button>
          <button v-if="o.status === 'paid_escrow' && role === 'buyer'" class="btn btn-outline btn-sm" @click="$router.push(`/order/${o.id}`)">申请退款</button>
          <button v-if="o.status === 'completed'" class="btn btn-outline btn-sm" @click="$router.push('/my-reviews')">去评价</button>
          <button v-if="o.status === 'refunding'" class="btn btn-outline btn-sm" @click="$router.push(`/order/${o.id}`)">查看详情</button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state"><span class="icon">📭</span><p>暂无相关订单</p></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { orders, currentUser, getProduct, getUser, formatPrice, relativeTime } from '@/mock/data'
import StatusBadge from '@/components/StatusBadge.vue'

const role = ref('buyer')
const statusFilter = ref('')

const statuses = [
  { value: '', label: '全部' },
  { value: 'pending_payment', label: '待付款' },
  { value: 'paid_escrow', label: '已付款' },
  { value: 'completed', label: '已完成' },
  { value: 'refunding', label: '退款中' },
  { value: 'refunded', label: '已退款' },
]

const filteredOrders = computed(() => {
  let list = orders
  if (role.value === 'buyer') list = list.filter(o => o.buyerId === currentUser.id)
  else list = list.filter(o => o.sellerId === currentUser.id)
  if (statusFilter.value) list = list.filter(o => o.status === statusFilter.value)
  list = [...list].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
  return list
})

function counterpartyName(o: any) {
  const uid = role.value === 'buyer' ? o.sellerId : o.buyerId
  return getUser(uid)?.nickname || '未知'
}

function payOrder(o: any) { o.status = 'paid_escrow'; o.paidAt = new Date().toISOString(); showToast('支付成功！') }
function confirmReceipt(o: any) { o.status = 'completed'; o.completedAt = new Date().toISOString(); showToast('确认收货成功！') }
function showToast(msg: string) {
  const el = document.createElement('div'); el.className = 'toast'; el.textContent = msg
  document.body.appendChild(el); setTimeout(() => el.remove(), 3000)
}
</script>

<style scoped>
.page-title { font-size: 20px; margin-bottom: 16px; }
.tabs { display: flex; gap: 0; border-bottom: 2px solid var(--border-light); margin-bottom: 12px; }
.tab-item { padding: 10px 24px; cursor: pointer; color: var(--text-muted); border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all var(--transition); }
.tab-item.active { color: var(--primary); border-bottom-color: var(--primary); font-weight: 500; }
.filter-tabs { display: flex; gap: 8px; margin-bottom: 20px; flex-wrap: wrap; }
.filter-tab { padding: 4px 12px; border-radius: 14px; cursor: pointer; font-size: 13px; background: var(--bg-main); transition: all var(--transition); }
.filter-tab.active { background: var(--primary); color: white; }
.order-card { margin-bottom: 12px; cursor: pointer; transition: all var(--transition); }
.order-card:hover { box-shadow: var(--shadow-hover); }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid var(--border-light); }
.order-no { font-size: 13px; color: var(--text-muted); }
.order-body { display: flex; align-items: center; gap: 14px; }
.thumb { width: 56px; height: 56px; border-radius: var(--radius-sm); flex-shrink: 0; }
.order-info { flex: 1; }
.order-info h4 { font-size: 15px; margin-bottom: 4px; }
.order-counterparty { font-size: 13px; color: var(--text-muted); }
.order-time { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
.order-amount { font-size: 18px; font-weight: 700; color: var(--danger); }
.order-actions { display: flex; gap: 8px; margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--border-light); justify-content: flex-end; }
</style>