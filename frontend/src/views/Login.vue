<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <span class="logo">🎓</span>
        <h1>Campus2Hand</h1>
        <p>校园二手交易平台</p>
      </div>
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label>学号</label>
          <input v-model="studentId" type="text" placeholder="请输入学号" required />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" required />
        </div>
        <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
        <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <div class="login-footer">
        <span>测试账号：2024010101 / Pass1234</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { setAuth } from '@/store/auth'

const router = useRouter()
const studentId = ref('2024010101')
const password = ref('Pass1234')
const loading = ref(false)
const errorMsg = ref('')

async function handleLogin() {
  loading.value = true
  errorMsg.value = ''
  try {
    const data = await login({ studentId: studentId.value, password: password.value })
    setAuth(data.accessToken, data.refreshToken, data.user)
    router.push('/')
  } catch (e: any) {
    errorMsg.value = e.message || '登录失败，请检查学号和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1E3A5F, #2D5F8A);
}
.login-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  width: 400px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.login-header .logo {
  font-size: 48px;
}
.login-header h1 {
  font-size: 24px;
  color: #1E3A5F;
  margin: 8px 0;
}
.login-header p {
  color: #999;
  font-size: 14px;
}
.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}
.form-group input:focus {
  border-color: #4A90D9;
  box-shadow: 0 0 0 3px rgba(74, 144, 217, 0.1);
}
.error-msg {
  color: #FF4D4F;
  font-size: 13px;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #FFF2F0;
  border-radius: 4px;
}
.btn-block {
  width: 100%;
  padding: 12px;
  font-size: 16px;
}
.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #999;
  font-size: 12px;
}
</style>