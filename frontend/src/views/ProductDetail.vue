<template>
  <div v-if="product" class="detail-page">
    <div class="detail-main">
      <div class="detail-gallery">
        <ImageCarousel :images="product.imageUrls" />
      </div>
      <div class="detail-info">
        <h1 class="detail-title">{{ product.title }}</h1>
        <p class="detail-price">{{ formatPrice(product.price) }}</p>
        <div class="detail-tags">
          <span class="tag">{{ getConditionLabel(product.condition) }}</span>
          <span class="tag">{{ getCategoryLabel(product.category) }}</span>
          <span class="tag">{{ product.campus }}</span>
        </div>
        <div class="detail-desc">
          <h3>商品描述</h3>
          <p>{{ product.description }}</p>
        </div>
        <div class="seller-card card">
          <div class="seller-header">
            <div class="seller-avatar">{{ product.sellerNickname?.charAt(0) }}</div>
            <div class="seller-info">
              <h4>{{ product.sellerNickname }}</h4>
              <StarRating :rating="product.sellerReputationScore || 3" size="16px" />
            </div>
          </div>
          <div class="seller-stats">
            <div class="stat"><span class="stat-num">{{ product.totalSold || 0 }}</span><span class="stat-label">成交</span></div>
            <div class="stat"><span class="stat-num">{{ product.reviewCount || 0 }}</span><span class="stat-label">评价</span></div>
            <div class="stat"><span class="stat-num">{{ product.campus }}</span><span class="stat-label">校区</span></div>
          </div>
        </div>
        <div class="detail-actions">
          <button class="btn btn-primary btn-lg" @click="contactSeller">💬 联系卖家</button>
          <button class="btn btn-danger btn-lg" @click="buyNow" :disabled="buying">{{ buying ? '处理中...' : '🛒 立即购买' }}</button>
        </div>
      </div>
    </div>

    <div class="reviews-section card">
      <h3>卖家评价 ({{ sellerReviews.length }})</h3>
      <div v-if="sellerReviews.length" class="review-list">
        <div v-for="r in sellerReviews" :key="r.id" class="review-item">
          <div class="review-header">
            <div class="review-avatar">{{ r.reviewerNickname?.charAt(0) || '?' }}</div>
            <div class="review-meta">
              <span class="review-name">{{ r.reviewerNickname }}</span>
              <StarRating :rating="r.rating" size="12px" />
            </div>
            <span class="review-time">{{ relativeTime(r.createdAt) }}</span>
          </div>
          <p class="review-content">{{ r.content }}</p>
          <div v-if="r.tags?.length" class="review-tags">
            <span v-for="t in r.tags" :key="t" class="tag">{{ t }}</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-state"><p>暂无评价</p></div>
    </div>
  </div>
  <div v-else-if="loading" class="loading-state"><p>加载中...</p></div>
  <div v-else class="empty-state"><span class="icon">😕</span><p>商品不存在或已下架</p></div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, ProductDetail } from '@/api/product'
import { createOrder } from '@/api/order'
import { formatPrice, getConditionLabel, getCategoryLabel, relativeTime } from '@/utils/format'
import ImageCarousel from '@/components/ImageCarousel.vue'
import StarRating from '@/components/StarRating.vue'

const route = useRoute()
const router = useRouter()
const product = ref<ProductDetail | null>(null)
const loading = ref(true)
const buying = ref(false)
const sellerReviews = ref<any[]>([])

async function fetchProduct() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    product.value = await getProductDetail(id)
  } catch (e) {
    console.error('获取商品详情失败', e)
    product.value = null
  } finally {
    loading.value = false
  }
}

function contactSeller() {
  router.push('/messages')
}

async function buyNow() {
  if (!product.value) return
  buying.value = true
  try {
    const order = await createOrder(product.value.id)
    router.push(`/order/${order.id}`)
  } catch (e: any) {
    alert(e.message || '下单失败')
  } finally {
    buying.value = false
  }
}

onMounted(() => { fetchProduct() })
</script>

<style scoped>
.detail-page { max-width: 1200px; margin: 0 auto; }
.detail-main { display: flex; gap: 24px; margin-bottom: 24px; }
.detail-gallery { width: 480px; flex-shrink: 0; }
.detail-info { flex: 1; }
.detail-title { font-size: 22px; margin-bottom: 12px; line-height: 1.4; }
.detail-price { font-size: 28px; font-weight: 700; color: var(--danger); margin-bottom: 12px; }
.detail-tags { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.detail-desc { margin-bottom: 20px; }
.detail-desc h3 { font-size: 15px; margin-bottom: 8px; }
.detail-desc p { color: var(--text-secondary); line-height: 1.7; white-space: pre-wrap; }
.seller-card { margin-bottom: 20px; }
.seller-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.seller-avatar { width: 44px; height: 44px; border-radius: 50%; background: var(--primary-light); color: white; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: 600; }
.seller-info h4 { font-size: 15px; }
.seller-stats { display: flex; gap: 24px; }
.stat { display: flex; flex-direction: column; align-items: center; }
.stat-num { font-size: 18px; font-weight: 600; color: var(--primary); }
.stat-label { font-size: 12px; color: var(--text-muted); }
.detail-actions { display: flex; gap: 12px; }
.reviews-section { margin-top: 20px; }
.reviews-section h3 { margin-bottom: 16px; }
.review-item { padding: 14px 0; border-bottom: 1px solid var(--border-light); }
.review-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.review-avatar { width: 32px; height: 32px; border-radius: 50%; background: var(--bg-main); display: flex; align-items: center; justify-content: center; font-size: 13px; color: var(--text-muted); }
.review-meta { flex: 1; }
.review-name { font-size: 13px; font-weight: 500; margin-right: 6px; }
.review-time { font-size: 12px; color: var(--text-muted); }
.review-content { font-size: 14px; color: var(--text-secondary); line-height: 1.6; }
.review-tags { display: flex; gap: 6px; margin-top: 6px; flex-wrap: wrap; }
@media (max-width: 900px) { .detail-main { flex-direction: column; } .detail-gallery { width: 100%; } }
</style>