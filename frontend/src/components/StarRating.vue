<template>
  <div class="star-rating" :style="{ fontSize: size }">
    <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= Math.floor(rating) }">
      {{ i <= Math.floor(rating) ? '★' : '☆' }}
    </span>
    <span class="rating-value">{{ rating.toFixed(1) }}</span>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ rating: number; size?: string }>(), { size: '14px' })
</script>

<style scoped>
.star-rating { display: inline-flex; align-items: center; gap: 1px; }
.star { color: #e0e0e0; }
.star.filled { color: #f5a623; }
.rating-value { margin-left: 4px; color: var(--text-muted); font-size: 12px; }
</style>