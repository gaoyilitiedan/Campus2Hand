<template>
  <div class="disputes-page">
    <h2 class="page-title">⚠️ 投诉工单</h2>

    <div v-if="disputes.length" class="dispute-list">
      <div v-for="d in disputes" :key="d.id" class="dispute-card card" @click="openDetail(d)">
        <div class="dispute-header">
          <span class="dispute-id">工单号: {{ d.id }}</span>
          <StatusBadge :status="d.status" />
        </div>
        <div class="dispute-type">
          <span class="tag">{{ typeLabel(d.type) }}</span>
          <span class="dispute-order" @click.stop="$router.push(`/order/${d.orderId}`)">关联订单 →</span>
        </div>
        <p class="dispute-desc">{{ d.description }}</p>
        <p class="dispute-time">{{ relativeTime(d.createdAt) }}</p>
      </div>
    </div>
    <div v-else class="empty-state"><span class="icon">✅</span><p>暂无投诉工单</p></div>

    <div v-if="activeDispute" class="modal-overlay" @click.self="activeDispute = null">
      <div class="modal-content" style="min-width:560px">
        <h3 class="modal-title">工单详情 - {{ activeDispute.id }}</h3>
        <StatusBadge :status="activeDispute.status" style="margin-bottom:12px" />
        <p style="margin-bottom:12px;line-height:1.6">{{ activeDispute.description }}</p>

        <h4 style="margin-bottom:8px">留言记录</h4>
        <div class="comment-list">
          <div v-for="c in activeDispute.comments" :key="c.id" class="comment-item">
            <div class="comment-avatar">{{ getUser(c.senderId)?.nickname?.charAt(0) }}</div>
            <div class="comment-body">
              <div class="comment-header"><strong>{{ getUser(c.senderId)?.nickname }}</strong><span>{{ relativeTime(c.createdAt) }}</span></div>
              <p>{{ c.content }}</p>
            </div>
          </div>
        </div>

        <div v-if="activeDispute.status === 'processing'" style="margin-top:12px">
          <input v-model="commentText" placeholder="追加留言..." style="width:100%;padding:10px;border:1px solid var(--border);border-radius:var(--radius-sm);margin-bottom:8px" @keyup.enter="addComment" />
          <button class="btn btn-primary btn-sm" @click="addComment" :disabled="!commentText.trim()">发送</button>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="activeDispute = null">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { disputes, getUser, relativeTime } from '@/mock/data'
import StatusBadge from '@/components/StatusBadge.vue'

const activeDispute = ref<any>(null)
const commentText = ref('')

const typeLabels: Record<string, string> = {
  product_issue: '商品问题', payment_issue: '支付问题', behavior_violation: '行为违规', other: '其他'
}
function typeLabel(t: string) { return typeLabels[t] || t }

function openDetail(d: any) { activeDispute.value = d }

function addComment() {
  if (!commentText.value.trim() || !activeDispute.value) return
  activeDispute.value.comments.push({
    id: `dc_${Date.now()}`,
    senderId: 'u_001',
    content: commentText.value.trim(),
    createdAt: new Date().toISOString()
  })
  commentText.value = ''
}
</script>

<style scoped>
.page-title { font-size: 20px; margin-bottom: 20px; }
.dispute-card { margin-bottom: 14px; cursor: pointer; transition: all var(--transition); }
.dispute-card:hover { box-shadow: var(--shadow-hover); }
.dispute-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.dispute-id { font-size: 13px; color: var(--text-muted); }
.dispute-type { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.dispute-order { font-size: 13px; color: var(--primary); cursor: pointer; }
.dispute-desc { font-size: 14px; color: var(--text-secondary); line-height: 1.6; margin-bottom: 8px; }
.dispute-time { font-size: 12px; color: var(--text-muted); }
.comment-list { max-height: 240px; overflow-y: auto; margin-bottom: 8px; }
.comment-item { display: flex; gap: 10px; padding: 10px 0; border-bottom: 1px solid var(--border-light); }
.comment-avatar { width: 32px; height: 32px; border-radius: 50%; background: var(--bg-main); display: flex; align-items: center; justify-content: center; font-size: 12px; color: var(--text-muted); flex-shrink: 0; }
.comment-header { display: flex; justify-content: space-between; align-items: center; font-size: 13px; margin-bottom: 4px; }
.comment-header span { font-size: 11px; color: var(--text-muted); }
.comment-body p { font-size: 13px; color: var(--text-secondary); }
</style>