<template>
  <div class="carousel" v-if="images.length">
    <div class="carousel-track" :style="{ transform: `translateX(-${current * 100}%)` }">
      <div v-for="(img, idx) in images" :key="idx" class="carousel-slide" :style="{ backgroundColor: img }">
        <span class="slide-label">{{ idx + 1 }} / {{ images.length }}</span>
      </div>
    </div>
    <button v-if="images.length > 1" class="carousel-btn prev" @click="prev">❮</button>
    <button v-if="images.length > 1" class="carousel-btn next" @click="next">❯</button>
    <div v-if="images.length > 1" class="carousel-dots">
      <span v-for="(_, idx) in images" :key="idx" class="dot" :class="{ active: idx === current }" @click="current = idx"></span>
    </div>
  </div>
  <div v-else class="carousel-empty">暂无图片</div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
const props = defineProps<{ images: string[] }>()
const current = ref(0)
function prev() { current.value = (current.value - 1 + props.images.length) % props.images.length }
function next() { current.value = (current.value + 1) % props.images.length }
</script>

<style scoped>
.carousel { position: relative; overflow: hidden; border-radius: var(--radius); aspect-ratio: 4/3; background: #f0f0f0; }
.carousel-track { display: flex; transition: transform 0.4s ease; height: 100%; }
.carousel-slide { min-width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 48px; color: rgba(255,255,255,0.6); position: relative; }
.slide-label { position: absolute; bottom: 8px; right: 12px; font-size: 12px; color: rgba(255,255,255,0.8); }
.carousel-btn { position: absolute; top: 50%; transform: translateY(-50%); background: rgba(0,0,0,0.3); color: white; border: none; width: 36px; height: 36px; border-radius: 50%; font-size: 16px; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.carousel-btn:hover { background: rgba(0,0,0,0.5); }
.prev { left: 10px; }
.next { right: 10px; }
.carousel-dots { position: absolute; bottom: 10px; left: 50%; transform: translateX(-50%); display: flex; gap: 6px; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: rgba(255,255,255,0.5); cursor: pointer; }
.dot.active { background: white; }
.carousel-empty { aspect-ratio: 4/3; background: #f0f0f0; display: flex; align-items: center; justify-content: center; color: var(--text-muted); border-radius: var(--radius); }
</style>