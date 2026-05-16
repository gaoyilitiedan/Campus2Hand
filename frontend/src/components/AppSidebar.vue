<template>
  <aside class="sidebar" :class="{ collapsed: isCollapsed }">
    <div class="sidebar-toggle" @click="isCollapsed = !isCollapsed">
      {{ isCollapsed ? '▶' : '◀' }}
    </div>
    <nav class="sidebar-nav">
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
      >
        <span class="nav-icon">{{ item.icon }}</span>
        <span v-if="!isCollapsed" class="nav-label">{{ item.label }}</span>
      </router-link>
    </nav>
    <div v-if="!isCollapsed" class="sidebar-footer">
      <div class="footer-user">
        <div class="footer-avatar">{{ currentUser.nickname.charAt(0) }}</div>
        <div class="footer-info">
          <div class="footer-name">{{ currentUser.nickname }}</div>
          <div class="footer-score">信誉分 ⭐{{ currentUser.reputationScore }}</div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { currentUser } from '@/mock/data'

const route = useRoute()
const isCollapsed = ref(false)

const menuItems = [
  { path: '/', label: '商品市场', icon: '📦' },
  { path: '/publish', label: '发布商品', icon: '📝' },
  { path: '/my-products', label: '我的商品', icon: '📋' },
  { path: '/my-orders', label: '我的订单', icon: '🛒' },
  { path: '/messages', label: '聊天消息', icon: '💬' },
  { path: '/my-reviews', label: '评价管理', icon: '⭐' },
  { path: '/my-disputes', label: '投诉工单', icon: '⚠️' },
  { path: '/profile', label: '个人中心', icon: '👤' },
]

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-w);
  background: var(--bg-sidebar);
  color: #cbd5e1;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width var(--transition);
  position: relative;
  overflow: hidden;
}
.sidebar.collapsed {
  width: 60px;
}
.sidebar-toggle {
  position: absolute;
  top: 12px;
  right: 8px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  font-size: 10px;
  color: #64748b;
  z-index: 10;
}
.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.08);
}
.sidebar-nav {
  flex: 1;
  padding: 40px 0 0;
  overflow-y: auto;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: #94a3b8;
  transition: all var(--transition);
  border-left: 3px solid transparent;
  white-space: nowrap;
}
.nav-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #e2e8f0;
}
.nav-item.active {
  background: rgba(74, 144, 217, 0.15);
  color: var(--primary-light);
  border-left-color: var(--primary-light);
}
.nav-icon {
  font-size: 18px;
  flex-shrink: 0;
  width: 24px;
  text-align: center;
}
.nav-label {
  font-size: 14px;
}
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}
.footer-user {
  display: flex;
  align-items: center;
  gap: 10px;
}
.footer-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--primary-light);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}
.footer-name {
  font-size: 13px;
  color: #e2e8f0;
}
.footer-score {
  font-size: 11px;
  color: #fbbf24;
}
</style>