<template>
  <header class="header">
    <div class="header-left">
      <span class="header-logo">🎓</span>
      <h1 class="header-title">Campus2Hand 校园二手交易平台</h1>
    </div>
    <div class="header-right">
      <div class="user-dropdown" @click="toggleDropdown">
        <div class="user-avatar">{{ userAvatar }}</div>
        <span class="user-name">{{ authState.user?.nickname }}</span>
        <span class="arrow">▾</span>
      </div>
      <div v-if="showDropdown" class="dropdown-menu" @click.stop>
        <router-link to="/profile" class="dropdown-item" @click="showDropdown = false">👤 个人中心</router-link>
        <router-link to="/my-orders" class="dropdown-item" @click="showDropdown = false">🛒 我的订单</router-link>
        <div class="dropdown-divider"></div>
        <div class="dropdown-item logout" @click="handleLogout">🚪 退出登录</div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authState, clearAuth } from '@/store/auth'

const router = useRouter()
const showDropdown = ref(false)

const userAvatar = computed(() => authState.user?.nickname?.charAt(0) || '?')

function toggleDropdown() {
  showDropdown.value = !showDropdown.value
}

function handleLogout() {
  showDropdown.value = false
  clearAuth()
  router.push('/login')
}

document.addEventListener('click', () => { showDropdown.value = false })
</script>

<style scoped>
.header {
  height: var(--header-h);
  background: linear-gradient(135deg, #1E3A5F, #2D5F8A);
  color: var(--text-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.header-logo {
  font-size: 24px;
}
.header-title {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}
.header-right {
  position: relative;
}
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: background var(--transition);
}
.user-dropdown:hover {
  background: rgba(255, 255, 255, 0.15);
}
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}
.user-name {
  font-size: 14px;
}
.arrow {
  font-size: 10px;
  opacity: 0.7;
}
.dropdown-menu {
  position: absolute;
  top: 44px;
  right: 0;
  background: var(--bg-card);
  border-radius: var(--radius);
  box-shadow: var(--shadow-hover);
  min-width: 160px;
  padding: 8px 0;
  z-index: 200;
}
.dropdown-item {
  display: block;
  padding: 8px 16px;
  color: var(--text-main);
  font-size: 14px;
  transition: background var(--transition);
}
.dropdown-item:hover {
  background: var(--bg-main);
}
.dropdown-divider {
  height: 1px;
  background: var(--border);
  margin: 4px 0;
}
.logout {
  color: var(--text-muted);
  cursor: pointer;
}
</style>