<template>
  <div class="home">
    <div class="search-section card">
      <div class="search-row">
        <input v-model="keyword" placeholder="🔍 搜索商品标题、描述..." class="search-input" @input="onSearch" />
      </div>
      <div class="filter-tabs">
        <span v-for="cat in categories" :key="cat.value" class="filter-tab" :class="{ active: category === cat.value }" @click="category = cat.value; onSearch()">{{ cat.label }}</span>
      </div>
      <div class="filter-row">
        <input v-model.number="minPrice" type="number" placeholder="最低价" class="filter-input" @input="onSearch" />
        <span class="filter-sep">—</span>
        <input v-model.number="maxPrice" type="number" placeholder="最高价" class="filter-input" @input="onSearch" />
        <select v-model="condition" class="filter-select" @change="onSearch">
          <option value="">新旧程度</option>
          <option value="brand_new">全新</option>
          <option value="like_new">几乎全新</option>
          <option value="slightly_used">轻微使用痕迹</option>
          <option value="visibly_used">明显使用痕迹</option>
        </select>
        <select v-model="campus" class="filter-select" @change="onSearch">
          <option value="">全部校区</option>
          <option v-for="c in campusList" :key="c" :value="c">{{ c }}</option>
        </select>
        <select v-model="sortBy" class="filter-select" @change="onSearch">
          <option value="created_at">最新发布</option>
          <option value="price_asc">价格从低到高</option>
          <option value="price_desc">价格从高到低</option>
          <option value="reputation_desc">信誉优先</option>
        </select>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <p>加载中...</p>
    </div>

    <div v-else-if="products.length" class="product-grid">
      <ProductCard v-for="p in products" :key="p.id" :product="p" @click="goDetail(p.id)" />
    </div>

    <div v-else class="empty-state">
      <span class="icon">🔍</span>
      <p>没有找到匹配的商品，试试其他关键词吧</p>
    </div>

    <Pagination v-model="page" :total-pages="totalPages" @update:model-value="onPageChange" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchProducts, ProductItem } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'
import Pagination from '@/components/Pagination.vue'

const router = useRouter()
const keyword = ref('')
const category = ref('')
const minPrice = ref<number | null>(null)
const maxPrice = ref<number | null>(null)
const condition = ref('')
const campus = ref('')
const sortBy = ref('created_at')
const page = ref(1)
const pageSize = 12
const totalPages = ref(1)
const loading = ref(false)
const products = ref<ProductItem[]>([])

const categories = [
  { value: '', label: '全部' },
  { value: 'textbook', label: '教材' },
  { value: 'digital', label: '数码' },
  { value: 'lifestyle', label: '生活用品' },
  { value: 'sports', label: '运动器材' },
  { value: 'clothing', label: '服饰' },
  { value: 'other', label: '其他' },
]

const campusList = ['本部校区', '东校区', '西校区', '北校区']

let searchTimer: ReturnType<typeof setTimeout> | null = null

function onSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    fetchProducts()
  }, 300)
}

function onPageChange(newPage: number) {
  page.value = newPage
  fetchProducts()
}

async function fetchProducts() {
  loading.value = true
  try {
    let sortOrder: string | undefined
    let sortField: string | undefined
    if (sortBy.value === 'price_asc') { sortField = 'price'; sortOrder = 'asc' }
    else if (sortBy.value === 'price_desc') { sortField = 'price'; sortOrder = 'desc' }
    else if (sortBy.value === 'reputation_desc') { sortField = 'reputation'; sortOrder = 'desc' }

    const result = await searchProducts({
      page: page.value,
      size: pageSize,
      keyword: keyword.value || undefined,
      category: category.value || undefined,
      condition: condition.value || undefined,
      campus: campus.value || undefined,
      minPrice: minPrice.value ? minPrice.value * 100 : undefined,
      maxPrice: maxPrice.value ? maxPrice.value * 100 : undefined,
      sortBy: sortField,
      sortOrder
    })
    products.value = result.list
    totalPages.value = result.pages
  } catch (e) {
    console.error('获取商品列表失败', e)
    products.value = []
  } finally {
    loading.value = false
  }
}

function goDetail(id: number) { router.push(`/product/${id}`) }

onMounted(() => { fetchProducts() })
</script>

<style scoped>
.home { max-width: 1400px; margin: 0 auto; }
.search-input { width: 100%; padding: 12px 16px; border: 2px solid var(--border); border-radius: var(--radius); font-size: 15px; transition: border-color var(--transition); }
.search-input:focus { border-color: var(--primary); }
.search-row { margin-bottom: 12px; }
.filter-tabs { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.filter-tab { padding: 6px 14px; border-radius: 16px; cursor: pointer; font-size: 13px; background: var(--bg-main); transition: all var(--transition); }
.filter-tab:hover { color: var(--primary); }
.filter-tab.active { background: var(--primary); color: white; }
.filter-row { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.filter-input { width: 110px; padding: 8px 10px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: 13px; }
.filter-sep { color: var(--text-muted); }
.filter-select { padding: 8px 10px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: 13px; background: white; cursor: pointer; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-top: 20px; }
@media (max-width: 1200px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
</style>