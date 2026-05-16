<template>
  <div class="product-card" @click="$emit('click')">
    <div class="card-img" :style="{ backgroundImage: product.coverImage ? `url(${product.coverImage})` : 'none', backgroundColor: product.coverImage ? '#f0f0f0' : '#e0e0e0', backgroundSize: 'cover', backgroundPosition: 'center' }">
      <span v-if="!product.coverImage" class="no-img">📷</span>
    </div>
    <div class="card-body">
      <h3 class="card-title">{{ product.title }}</h3>
      <p class="card-price">{{ formatPrice(product.price) }}</p>
      <div class="card-meta">
        <span class="condition-tag">{{ getConditionLabel(product.condition) }}</span>
        <span class="seller">{{ product.sellerNickname }}</span>
      </div>
      <div class="card-footer">
        <StarRating :rating="product.sellerReputationScore || 3" size="12px" />
        <span class="time">{{ relativeTime(product.createdAt) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getConditionLabel, formatPrice, relativeTime } from '@/utils/format'
import StarRating from './StarRating.vue'
defineProps<{ product: any }>()
defineEmits<{ (e: 'click'): void }>()
</script>

<style scoped>
.product-card { background: var(--bg-card); border-radius: var(--radius); overflow: hidden; cursor: pointer; transition: all var(--transition); box-shadow: var(--shadow); }
.product-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-hover); }
.card-img { height: 160px; display: flex; align-items: center; justify-content: center; }
.no-img { font-size: 40px; opacity: 0.5; }
.card-body { padding: 14px; }
.card-title { font-size: 14px; font-weight: 500; line-height: 1.4; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 8px; }
.card-price { font-size: 20px; font-weight: 700; color: var(--danger); margin-bottom: 8px; }
.card-meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.condition-tag { font-size: 11px; padding: 2px 6px; border-radius: var(--radius-sm); background: rgba(74,144,217,0.08); color: var(--primary); border: 1px solid rgba(74,144,217,0.2); }
.seller { font-size: 12px; color: var(--text-muted); }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.time { font-size: 11px; color: var(--text-muted); }
</style>