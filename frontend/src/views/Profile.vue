<template>
  <div class="profile-page">
    <div v-if="loading" class="loading-state"><p>加载中...</p></div>
    <template v-else-if="profile">
      <div class="card profile-header">
        <div class="profile-avatar">{{ profile.nickname?.charAt(0) }}</div>
        <div class="profile-name-group">
          <h2>{{ profile.nickname }}</h2>
          <div class="profile-score">⭐ {{ profile.reputationScore }} <span class="score-label">信誉分</span></div>
        </div>
        <span class="profile-campus">{{ profile.campus }}</span>
      </div>

      <div class="card stats-row">
        <div class="stat-item">
          <span class="stat-num">{{ profile.productCount || 0 }}</span>
          <span class="stat-label">发布数</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-num">{{ (profile.soldCount || 0) + (profile.boughtCount || 0) }}</span>
          <span class="stat-label">成交数</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-num">{{ profile.reviewCount || 0 }}</span>
          <span class="stat-label">评价数</span>
        </div>
      </div>

      <div class="card info-card">
        <h3>个人信息</h3>
        <div class="info-item">
          <span class="info-label">学号</span>
          <span class="info-value">{{ profile.studentId }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">校园邮箱</span>
          <span class="info-value">{{ profile.email }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">手机号</span>
          <span class="info-value">{{ profile.phone || '未绑定' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">校区</span>
          <span class="info-value">{{ profile.campus }}</span>
        </div>
      </div>

      <div class="card action-card">
        <h3>账户操作</h3>
        <div class="action-list">
          <button class="btn btn-outline" @click="showToast('修改密码（演示模式）')">🔒 修改密码</button>
          <button class="btn btn-outline" @click="showCampusPicker = true">🏫 切换校区</button>
          <button class="btn btn-outline" style="color:var(--danger)" @click="handleLogout">🚪 退出登录</button>
        </div>
      </div>

      <div v-if="showCampusPicker" class="modal-overlay" @click.self="showCampusPicker = false">
        <div class="modal-content" style="min-width:360px">
          <h3 class="modal-title">切换校区</h3>
          <div style="display:flex;flex-direction:column;gap:8px">
            <button v-for="c in campusList" :key="c" class="btn" :class="c === profile.campus ? 'btn-primary' : 'btn-outline'" @click="changeCampus(c)">{{ c }}</button>
          </div>
          <div class="modal-footer">
            <button class="btn btn-outline" @click="showCampusPicker = false">取消</button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProfile } from '@/api/auth'
import { authState, clearAuth } from '@/store/auth'

const router = useRouter()
const profile = ref<any>(null)
const loading = ref(true)
const showCampusPicker = ref(false)

const campusList = ['本部校区', '东校区', '西校区', '北校区']

async function fetchProfile() {
  loading.value = true
  try {
    profile.value = await getProfile()
  } catch (e) {
    console.error('获取个人信息失败', e)
    profile.value = authState.user
  } finally {
    loading.value = false
  }
}

function changeCampus(c: string) {
  if (profile.value) profile.value.campus = c
  showCampusPicker.value = false
  showToast('校区已切换为：' + c)
}

function handleLogout() {
  clearAuth()
  router.push('/login')
}

function showToast(msg: string) {
  const el = document.createElement('div'); el.className = 'toast'; el.textContent = msg
  document.body.appendChild(el); setTimeout(() => el.remove(), 3000)
}

onMounted(() => { fetchProfile() })
</script>

<style scoped>
.profile-page { max-width: 640px; margin: 0 auto; }
.profile-header { display: flex; align-items: center; gap: 20px; margin-bottom: 16px; }
.profile-avatar { width: 72px; height: 72px; border-radius: 50%; background: linear-gradient(135deg, var(--primary), var(--primary-light)); color: white; display: flex; align-items: center; justify-content: center; font-size: 32px; font-weight: 600; }
.profile-name-group h2 { font-size: 22px; }
.profile-score { font-size: 18px; font-weight: 600; color: #f5a623; }
.score-label { font-size: 13px; color: var(--text-muted); font-weight: 400; }
.profile-campus { margin-left: auto; font-size: 14px; padding: 6px 14px; background: rgba(74,144,217,0.08); border-radius: 16px; color: var(--primary); }
.stats-row { display: flex; align-items: center; margin-bottom: 16px; }
.stat-item { flex: 1; text-align: center; display: flex; flex-direction: column; gap: 4px; }
.stat-num { font-size: 28px; font-weight: 700; color: var(--primary); }
.stat-label { font-size: 13px; color: var(--text-muted); }
.stat-divider { width: 1px; height: 40px; background: var(--border); }
.info-card, .action-card { margin-bottom: 16px; }
.info-card h3, .action-card h3 { font-size: 15px; margin-bottom: 14px; }
.info-item { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--border-light); font-size: 14px; }
.info-item:last-child { border-bottom: none; }
.info-label { color: var(--text-muted); }
.info-value { font-weight: 500; }
.action-list { display: flex; flex-direction: column; gap: 10px; }
.action-list button { justify-content: flex-start; width: 100%; text-align: left; }
</style>