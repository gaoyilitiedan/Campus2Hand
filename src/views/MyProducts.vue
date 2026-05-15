<template>
  <div class="my-products">
    <div class="page-header">
      <h2>📋 我的商品</h2>
      <button class="btn btn-primary" @click="$router.push('/publish')">+ 发布商品</button>
    </div>
    <div class="tabs">
      <span v-for="tab in tabs" :key="tab.value" class="tab-item" :class="{ active: activeTab === tab.value }" @click="activeTab = tab.value">{{ tab.label }} ({{ tab.count }})</span>
    </div>

    <div v-if="filteredList.length" class="product-table">
      <div class="table-header">
        <span class="col-img"></span>
        <span class="col-title">商品信息</span>
        <span class="col-price">价格</span>
        <span class="col-status">状态</span>
        <span class="col-view">浏览</span>
        <span class="col-action">操作</span>
      </div>
      <div v-for="p in filteredList" :key="p.id" class="table-row">
        <div class="col-img">
          <div class="thumb" :style="{ backgroundColor: p.images?.[0] || '#e0e0e0' }"></div>
        </div>
        <div class="col-title">{{ p.title }}</div>
        <div class="col-price">{{ formatPrice(p.price) }}</div>
        <div class="col-status"><StatusBadge :status="p.status" /></div>
        <div class="col-view">{{ p.viewCount }}</div>
        <div class="col-action">
          <button v-if="['on_sale', 'pending_review'].includes(p.status)" class="btn btn-sm btn-outline" @click="$router.push('/publish')">编辑</button>
          <button v-if="p.status === 'on_sale'" class="btn btn-sm btn-outline" style="color:var(--warning)" @click="delist(p.id)">下架</button>
          <button v-if="p.status === 'on_sale'" class="btn btn-sm btn-outline" style="color:var(--success)" @click="markSold(p.id)">标售出</button>
          <button v-if="p.status === 'delisted'" class="btn btn-sm btn-outline" style="color:var(--danger)" @click="deleteProduct(p.id)">删除</button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <span class="icon">📦</span>
      <p>暂无{{ getStatusLabel(activeTab) }}商品</p>
      <button v-if="activeTab === 'on_sale'" class="btn btn-primary" @click="$router.push('/publish')">去发布一个吧~</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { products, currentUser, updateProductStatus, formatPrice, getStatusLabel } from '@/mock/data'
import StatusBadge from '@/components/StatusBadge.vue'

const activeTab = ref('on_sale')

const myProducts = computed(() => products.filter(p => p.sellerId === currentUser.id))
const filteredList = computed(() => myProducts.value.filter(p => p.status === activeTab.value))

const tabs = computed(() => {
  const counts: Record<string, number> = {}
  myProducts.value.forEach(p => { counts[p.status] = (counts[p.status] || 0) + 1 })
  return [
    { value: 'on_sale', label: '在售', count: counts['on_sale'] || 0 },
    { value: 'pending_review', label: '审核中', count: counts['pending_review'] || 0 },
    { value: 'sold', label: '已售出', count: counts['sold'] || 0 },
    { value: 'delisted', label: '已下架', count: counts['delisted'] || 0 },
  ]
})

function delist(id: string) { updateProductStatus(id, 'delisted'); showToast('已下架') }
function markSold(id: string) { updateProductStatus(id, 'sold'); showToast('已标记为售出') }
function deleteProduct(id: string) {
  const idx = products.findIndex(p => p.id === id)
  if (idx > -1) products.splice(idx, 1)
  showToast('已删除')
}
function showToast(msg: string) {
  const el = document.createElement('div')
  el.className = 'toast'
  el.textContent = msg
  document.body.appendChild(el)
  setTimeout(() => el.remove(), 3000)
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; }
.tabs { display: flex; gap: 16px; border-bottom: 2px solid var(--border-light); margin-bottom: 20px; }
.tab-item { padding: 10px 16px; cursor: pointer; color: var(--text-muted); border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all var(--transition); font-size: 14px; }
.tab-item:hover { color: var(--primary); }
.tab-item.active { color: var(--primary); border-bottom-color: var(--primary); font-weight: 500; }
.table-header { display: flex; align-items: center; padding: 10px 16px; background: var(--bg-main); border-radius: var(--radius-sm); margin-bottom: 8px; font-size: 12px; color: var(--text-muted); font-weight: 500; }
.table-row { display: flex; align-items: center; padding: 12px 16px; background: var(--bg-card); border-radius: var(--radius-sm); margin-bottom: 6px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); transition: all var(--transition); }
.table-row:hover { box-shadow: var(--shadow); }
.col-img { width: 52px; flex-shrink: 0; }
.thumb { width: 40px; height: 40px; border-radius: var(--radius-sm); }
.col-title { flex: 1; padding: 0 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 14px; }
.col-price { width: 100px; color: var(--danger); font-weight: 600; font-size: 14px; }
.col-status { width: 80px; }
.col-view { width: 60px; color: var(--text-muted); font-size: 13px; text-align: center; }
.col-action { width: 180px; display: flex; gap: 6px; justify-content: flex-end; }
</style>