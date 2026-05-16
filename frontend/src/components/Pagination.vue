<template>
  <div class="pagination" v-if="totalPages > 1">
    <button :disabled="modelValue <= 1" @click="go(modelValue - 1)">上一页</button>
    <span v-for="p in pages" :key="p" class="page-num" :class="{ active: p === modelValue }" @click="go(p)">{{ p }}</span>
    <button :disabled="modelValue >= totalPages" @click="go(modelValue + 1)">下一页</button>
    <span class="total">共 {{ totalPages }} 页</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps<{ modelValue: number; totalPages: number }>()
const emit = defineEmits<{ (e: 'update:modelValue', v: number): void }>()
const pages = computed(() => {
  const res: number[] = []
  for (let i = Math.max(1, props.modelValue - 2); i <= Math.min(props.totalPages, props.modelValue + 2); i++) res.push(i)
  return res
})
function go(p: number) { if (p >= 1 && p <= props.totalPages) emit('update:modelValue', p) }
</script>

<style scoped>
.pagination { display: flex; align-items: center; justify-content: center; gap: 6px; margin-top: 24px; }
button { padding: 6px 14px; border: 1px solid var(--border); border-radius: var(--radius-sm); background: var(--bg-card); cursor: pointer; font-size: 13px; transition: all var(--transition); }
button:hover:not(:disabled) { border-color: var(--primary); color: var(--primary); }
button:disabled { opacity: 0.4; cursor: not-allowed; }
.page-num { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; border-radius: var(--radius-sm); cursor: pointer; font-size: 13px; transition: all var(--transition); }
.page-num:hover { background: rgba(74,144,217,0.1); }
.page-num.active { background: var(--primary); color: white; }
.total { margin-left: 8px; color: var(--text-muted); font-size: 13px; }
</style>