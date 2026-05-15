<template>
  <span class="status-badge" :class="badgeClass">{{ label }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getStatusLabel } from '@/mock/data'

const props = defineProps<{ status: string }>()
const label = computed(() => getStatusLabel(props.status))
const badgeClass = computed(() => {
  const s = props.status
  if (['on_sale', 'completed', 'resolved'].includes(s)) return 'badge-success'
  if (['pending_review', 'pending_payment', 'pending'].includes(s)) return 'badge-warning'
  if (['delisted', 'cancelled', 'rejected'].includes(s)) return 'badge-danger'
  if (['sold', 'paid_escrow', 'refunded'].includes(s)) return 'badge-primary'
  if (['refunding', 'processing'].includes(s)) return 'badge-info'
  return 'badge-info'
})
</script>

<style scoped>
.status-badge { display: inline-block; padding: 3px 10px; border-radius: 10px; font-size: 12px; white-space: nowrap; }
.badge-success { background: rgba(82,196,26,0.1); color: var(--success); }
.badge-warning { background: rgba(250,173,20,0.1); color: var(--warning); }
.badge-danger { background: rgba(255,77,79,0.1); color: var(--danger); }
.badge-primary { background: rgba(74,144,217,0.1); color: var(--primary); }
.badge-info { background: rgba(64,150,255,0.1); color: var(--info); }
</style>