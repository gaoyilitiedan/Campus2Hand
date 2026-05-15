<template>
  <div class="publish-page" v-if="!submitted">
    <h2 class="page-title">📝 发布闲置商品</h2>
    <div class="publish-layout">
      <div class="upload-section">
        <div class="upload-label">商品图片 <span class="hint">({{ images.length }}/9)</span></div>
        <div class="upload-grid">
          <div v-for="(img, idx) in images" :key="idx" class="upload-item" :style="{ backgroundColor: img }" @click="removeImage(idx)">
            <span class="remove-icon">✕</span>
          </div>
          <div v-if="images.length < 9" class="upload-add" @click="addImage">+</div>
        </div>
        <p class="upload-hint">点击 + 添加模拟图片，支持 JPG/PNG/WebP</p>
      </div>

      <div class="form-section">
        <div class="form-group">
          <label>商品标题 <span class="required">*</span></label>
          <input v-model="form.title" maxlength="60" placeholder="请输入商品标题" />
          <span class="char-count">{{ form.title.length }}/60</span>
        </div>
        <div class="form-group">
          <label>商品描述 <span class="required">*</span></label>
          <textarea v-model="form.description" maxlength="2000" rows="5" placeholder="请详细描述商品状况、使用时间等"></textarea>
          <span class="char-count">{{ form.description.length }}/2000</span>
        </div>
        <div class="form-group">
          <label>售价 <span class="required">*</span></label>
          <div class="price-input-wrap">
            <span class="price-prefix">¥</span>
            <input v-model.number="form.price" type="number" step="0.01" min="0.01" max="99999.99" placeholder="0.00" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group flex-1">
            <label>分类 <span class="required">*</span></label>
            <select v-model="form.category">
              <option value="">请选择分类</option>
              <option value="textbook">教材</option>
              <option value="digital">数码</option>
              <option value="lifestyle">生活用品</option>
              <option value="sports">运动器材</option>
              <option value="clothing">服饰</option>
              <option value="other">其他</option>
            </select>
          </div>
          <div class="form-group flex-1">
            <label>校区 <span class="required">*</span></label>
            <select v-model="form.campus">
              <option value="">请选择校区</option>
              <option v-for="c in campusList" :key="c" :value="c">{{ c }}</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label>新旧程度 <span class="required">*</span></label>
          <div class="radio-group">
            <label v-for="opt in conditions" :key="opt.value" class="radio-item" :class="{ active: form.condition === opt.value }">
              <input type="radio" v-model="form.condition" :value="opt.value" />
              <span>{{ opt.label }}</span>
            </label>
          </div>
        </div>

        <div class="form-actions">
          <button class="btn btn-primary btn-lg" @click="handleSubmit">发布商品</button>
          <button class="btn btn-outline btn-lg" @click="$router.push('/')">取消</button>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="success-page">
    <div class="success-card card">
      <span class="success-icon">✅</span>
      <h2>发布成功！</h2>
      <p>商品正在审核中，审核通过后将自动上架</p>
      <button class="btn btn-primary" @click="$router.push('/my-products')">查看我的商品</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { campusList, addProduct, currentUser } from '@/mock/data'

const submitted = ref(false)
const images = ref<string[]>([])
const colors = ['#4A90D9', '#7B68EE', '#52C41A', '#FAAD14', '#FF6B6B', '#45B7D1', '#F78FB3', '#786FA6', '#63CDDA', '#E77F67']

const form = reactive({
  title: '',
  description: '',
  price: null as number | null,
  category: '',
  campus: '',
  condition: '',
})

const conditions = [
  { value: 'brand_new', label: '全新' },
  { value: 'like_new', label: '几乎全新' },
  { value: 'slightly_used', label: '轻微使用痕迹' },
  { value: 'visibly_used', label: '明显使用痕迹' },
]

function addImage() {
  images.value.push(colors[Math.floor(Math.random() * colors.length)])
}

function removeImage(idx: number) {
  images.value.splice(idx, 1)
}

function handleSubmit() {
  if (!form.title || !form.description || !form.price || !form.category || !form.condition || !form.campus) {
    alert('请填写所有必填项')
    return
  }
  if (images.value.length === 0) {
    alert('请至少上传一张商品图片')
    return
  }
  addProduct({
    id: '',
    title: form.title,
    description: form.description,
    images: [...images.value],
    price: Math.round(form.price * 100),
    condition: form.condition as any,
    category: form.category as any,
    campus: form.campus,
    status: 'pending_review',
    viewCount: 0,
    sellerId: currentUser.id,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  })
  submitted.value = true
}
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 20px; }
.publish-layout { display: flex; gap: 24px; }
.upload-section { width: 320px; flex-shrink: 0; }
.upload-label { margin-bottom: 8px; font-weight: 500; }
.hint { color: var(--text-muted); font-weight: 400; font-size: 13px; }
.upload-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.upload-item, .upload-add { aspect-ratio: 1; border-radius: var(--radius); display: flex; align-items: center; justify-content: center; cursor: pointer; position: relative; }
.upload-add { border: 2px dashed var(--border); font-size: 32px; color: var(--text-muted); transition: all var(--transition); }
.upload-add:hover { border-color: var(--primary); color: var(--primary); }
.remove-icon { position: absolute; top: 4px; right: 4px; width: 20px; height: 20px; background: rgba(0,0,0,0.5); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 10px; opacity: 0; transition: opacity var(--transition); }
.upload-item:hover .remove-icon { opacity: 1; }
.upload-hint { font-size: 12px; color: var(--text-muted); margin-top: 8px; }
.form-section { flex: 1; }
.form-group { margin-bottom: 16px; position: relative; }
.form-group label { display: block; margin-bottom: 6px; font-weight: 500; font-size: 14px; }
.required { color: var(--danger); }
input, textarea, select { width: 100%; padding: 10px 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: 14px; background: white; }
textarea { resize: vertical; }
.char-count { position: absolute; right: 8px; bottom: -18px; font-size: 11px; color: var(--text-muted); }
.price-input-wrap { display: flex; align-items: stretch; border: 1px solid var(--border); border-radius: var(--radius-sm); overflow: hidden; }
.price-prefix { padding: 10px 12px; background: var(--bg-main); font-weight: 600; display: flex; align-items: center; }
.price-input-wrap input { border: none; border-radius: 0; }
.form-row { display: flex; gap: 16px; }
.flex-1 { flex: 1; }
.radio-group { display: flex; gap: 8px; flex-wrap: wrap; }
.radio-item { padding: 8px 16px; border: 1px solid var(--border); border-radius: var(--radius-sm); cursor: pointer; transition: all var(--transition); font-size: 13px; }
.radio-item input { display: none; }
.radio-item:hover { border-color: var(--primary); }
.radio-item.active { border-color: var(--primary); background: rgba(74,144,217,0.06); color: var(--primary); font-weight: 500; }
.form-actions { display: flex; gap: 12px; margin-top: 24px; }
.success-page { display: flex; align-items: center; justify-content: center; min-height: 60vh; }
.success-card { text-align: center; padding: 48px; max-width: 400px; }
.success-icon { font-size: 48px; display: block; margin-bottom: 16px; }
.success-card h2 { margin-bottom: 8px; }
.success-card p { color: var(--text-muted); margin-bottom: 20px; }
</style>