<template>
  <div class="reviews-page">
    <h2 class="page-title">⭐ 评价管理</h2>
    <div class="tabs">
      <span class="tab-item" :class="{ active: tab === 'received' }" @click="tab = 'received'">我收到的评价</span>
      <span class="tab-item" :class="{ active: tab === 'given' }" @click="tab = 'given'">我给出的评价</span>
    </div>

    <template v-if="tab === 'received'">
      <div class="card score-overview">
        <div class="score-big">
          <span class="score-num">{{ currentUser.reputationScore }}</span>
          <span class="score-label">信誉分</span>
        </div>
        <div class="score-dist">
          <div v-for="i in 5" :key="i" class="dist-row">
            <span class="dist-star">{{ i }}★</span>
            <div class="dist-bar-wrap"><div class="dist-bar" :style="{ width: distPercent(i) + '%' }"></div></div>
            <span class="dist-count">{{ distCount(i) }}</span>
          </div>
        </div>
      </div>

      <div v-if="receivedReviews.length" class="review-list">
        <div v-for="r in receivedReviews" :key="r.id" class="review-card card">
          <div class="review-top">
            <div class="review-avatar">{{ getUser(r.reviewerId)?.nickname?.charAt(0) }}</div>
            <div class="review-meta">
              <span class="review-name">{{ getUser(r.reviewerId)?.nickname }}</span>
              <StarRating :rating="r.rating" size="12px" />
            </div>
            <span class="review-time">{{ relativeTime(r.createdAt) }}</span>
          </div>
          <p class="review-text">{{ r.content }}</p>
          <div v-if="r.tags?.length" class="review-tags">
            <span v-for="t in r.tags" :key="t" class="tag">{{ t }}</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-state"><span class="icon">⭐</span><p>暂无评价</p></div>
    </template>

    <template v-if="tab === 'given'">
      <div v-if="givenReviews.length" class="review-list">
        <div v-for="r in givenReviews" :key="r.id" class="review-card card">
          <div class="review-top">
            <div class="review-avatar">{{ getUser(r.revieweeId)?.nickname?.charAt(0) }}</div>
            <div class="review-meta">
              <span class="review-name">{{ getUser(r.revieweeId)?.nickname }}</span>
              <StarRating :rating="r.rating" size="12px" />
            </div>
            <span class="review-time">{{ relativeTime(r.createdAt) }}</span>
          </div>
          <p class="review-text">{{ r.content }}</p>
          <div v-if="r.tags?.length" class="review-tags">
            <span v-for="t in r.tags" :key="t" class="tag">{{ t }}</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-state"><span class="icon">✍️</span><p>暂无给出的评价</p></div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { currentUser, reviews, getUser, getReviewsForUser, getReviewsByUser_given, relativeTime } from '@/mock/data'
import StarRating from '@/components/StarRating.vue'

const tab = ref('received')
const receivedReviews = computed(() => getReviewsForUser(currentUser.id))
const givenReviews = computed(() => getReviewsByUser_given(currentUser.id))

const distCount = (star: number) => receivedReviews.value.filter(r => r.rating === star).length
const distPercent = (star: number) => {
  const max = Math.max(...[1, 2, 3, 4, 5].map(distCount), 1)
  return (distCount(star) / max) * 100
}
</script>

<style scoped>
.page-title { font-size: 20px; margin-bottom: 16px; }
.tabs { display: flex; gap: 0; border-bottom: 2px solid var(--border-light); margin-bottom: 20px; }
.tab-item { padding: 10px 24px; cursor: pointer; color: var(--text-muted); border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all var(--transition); }
.tab-item.active { color: var(--primary); border-bottom-color: var(--primary); font-weight: 500; }
.score-overview { display: flex; align-items: center; gap: 40px; margin-bottom: 20px; }
.score-big { text-align: center; }
.score-num { font-size: 48px; font-weight: 700; color: var(--primary); display: block; }
.score-label { font-size: 13px; color: var(--text-muted); }
.score-dist { flex: 1; max-width: 300px; }
.dist-row { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.dist-star { font-size: 12px; width: 28px; color: var(--text-muted); }
.dist-bar-wrap { flex: 1; height: 8px; background: var(--bg-main); border-radius: 4px; overflow: hidden; }
.dist-bar { height: 100%; background: linear-gradient(90deg, #f5a623, #f7d774); border-radius: 4px; }
.dist-count { font-size: 12px; color: var(--text-muted); width: 20px; }
.review-card { margin-bottom: 12px; }
.review-top { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.review-avatar { width: 36px; height: 36px; border-radius: 50%; background: var(--primary-light); color: white; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 600; }
.review-meta { flex: 1; }
.review-name { font-weight: 500; font-size: 14px; margin-right: 8px; }
.review-time { font-size: 12px; color: var(--text-muted); }
.review-text { font-size: 14px; color: var(--text-secondary); line-height: 1.6; }
.review-tags { display: flex; gap: 6px; margin-top: 8px; flex-wrap: wrap; }
</style>