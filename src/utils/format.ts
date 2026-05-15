export function formatPrice(cents: number): string {
  const yuan = cents / 100
  if (yuan >= 10000) return `¥${(yuan / 10000).toFixed(1)}万`
  return `¥${yuan.toFixed(yuan % 1 === 0 ? 0 : 2)}`
}

export function getConditionLabel(condition: string): string {
  const map: Record<string, string> = {
    brand_new: '全新',
    like_new: '几乎全新',
    slightly_used: '轻微使用痕迹',
    visibly_used: '明显使用痕迹'
  }
  return map[condition] || condition
}

export function getCategoryLabel(category: string): string {
  const map: Record<string, string> = {
    textbook: '教材',
    digital: '数码',
    lifestyle: '生活用品',
    sports: '运动器材',
    clothing: '服饰',
    other: '其他'
  }
  return map[category] || category
}

export function getStatusLabel(status: string): string {
  const map: Record<string, string> = {
    pending_review: '待审核',
    on_sale: '在售',
    reserved: '已预定',
    sold: '已售出',
    delisted: '已下架',
    rejected: '审核未通过',
    pending_payment: '待付款',
    paid_escrow: '已付款',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消',
    refunding: '退款中',
    refunded: '已退款'
  }
  return map[status] || status
}

export function relativeTime(dateStr: string): string {
  const now = Date.now()
  const date = new Date(dateStr).getTime()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  const months = Math.floor(days / 30)
  if (months < 12) return `${months}个月前`
  return `${Math.floor(months / 12)}年前`
}

export function maskStudentId(id: string): string {
  if (!id || id.length < 4) return id
  return id.slice(0, 2) + '****' + id.slice(-2)
}

export function maskEmail(email: string): string {
  if (!email) return ''
  const [name, domain] = email.split('@')
  if (name.length <= 2) return name[0] + '***@' + domain
  return name[0] + '***' + name[name.length - 1] + '@' + domain
}

export function maskPhone(phone: string): string {
  if (!phone || phone.length < 7) return phone
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}