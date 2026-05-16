import { reactive } from 'vue'

export interface User {
  id: string
  studentId: string
  nickname: string
  avatar: string
  campus: string
  email: string
  phone: string
  reputationScore: number
  totalSold: number
  totalBought: number
  reviewCount: number
  createdAt: string
}

export interface Product {
  id: string
  title: string
  description: string
  images: string[]
  price: number
  condition: 'brand_new' | 'like_new' | 'slightly_used' | 'visibly_used'
  category: 'textbook' | 'digital' | 'lifestyle' | 'sports' | 'clothing' | 'other'
  campus: string
  status: 'on_sale' | 'pending_review' | 'sold' | 'delisted'
  viewCount: number
  sellerId: string
  createdAt: string
  updatedAt: string
}

export interface Order {
  id: string
  orderNo: string
  productId: string
  buyerId: string
  sellerId: string
  amount: number
  status: 'pending_payment' | 'paid_escrow' | 'completed' | 'refunding' | 'refunded' | 'cancelled'
  refundReason?: string
  refundDesc?: string
  createdAt: string
  paidAt?: string
  completedAt?: string
  expireAt?: string
}

export interface Conversation {
  id: string
  productId: string
  participantId: string
  unreadCount: number
  lastMessage: string
  lastMessageTime: string
  updatedAt: string
}

export interface Message {
  id: string
  conversationId: string
  senderId: string
  content: string
  createdAt: string
}

export interface Review {
  id: string
  orderId: string
  reviewerId: string
  revieweeId: string
  productId: string
  rating: number
  content: string
  tags: string[]
  isPublic: boolean
  createdAt: string
}

export interface Dispute {
  id: string
  orderId: string
  creatorId: string
  type: 'product_issue' | 'payment_issue' | 'behavior_violation' | 'other'
  description: string
  status: 'pending' | 'processing' | 'resolved' | 'rejected'
  comments: DisputeComment[]
  createdAt: string
}

export interface DisputeComment {
  id: string
  senderId: string
  content: string
  createdAt: string
}

const conditionLabels: Record<string, string> = {
  brand_new: '全新',
  like_new: '几乎全新',
  slightly_used: '轻微使用痕迹',
  visibly_used: '明显使用痕迹'
}

const categoryLabels: Record<string, string> = {
  textbook: '教材',
  digital: '数码',
  lifestyle: '生活用品',
  sports: '运动器材',
  clothing: '服饰',
  other: '其他'
}

const campusList = ['本部校区', '东校区', '西校区', '南校区']

const sellerNames = ['李四', '王五', '赵六', '孙七', '周八', '吴九', '郑十', '陈小明', '林小红', '黄小华']
const sellerPrefixes = ['同学', '学姐', '学长']

function rnd(min: number, max: number) { return Math.floor(Math.random() * (max - min + 1)) + min }

function randomSeller(idx: number): User {
  const id = `u_seller_${idx}`
  const name = sellerNames[idx % sellerNames.length]
  const prefix = sellerPrefixes[idx % sellerPrefixes.length]
  return {
    id,
    studentId: `2024${String(rnd(1, 50)).padStart(4, '0')}`,
    nickname: `${name}${prefix}`,
    avatar: '',
    campus: campusList[idx % campusList.length],
    email: `seller${idx}@campus.edu`,
    phone: `138${String(rnd(10000000, 99999999))}`,
    reputationScore: Number((rnd(30, 50) / 10).toFixed(1)),
    totalSold: rnd(3, 30),
    totalBought: rnd(1, 10),
    reviewCount: rnd(5, 40),
    createdAt: `2024-0${rnd(1, 9)}-${String(rnd(10, 28)).padStart(2, '0')}T08:00:00Z`
  }
}

export const currentUser: User = {
  id: 'u_001',
  studentId: '2024010101',
  nickname: '张三',
  avatar: '',
  campus: '本部校区',
  email: 'zhangsan@campus.edu',
  phone: '13812345678',
  reputationScore: 4.8,
  totalSold: 12,
  totalBought: 5,
  reviewCount: 18,
  createdAt: '2024-09-01T08:00:00Z'
}

const productTemplates = [
  { title: '高等数学（第七版）上下册', desc: '考研用书，上册有少量笔记，下册几乎全新。附赠习题全解一本。', price: 3500, cat: 'textbook' as const, cond: 'slightly_used' as const },
  { title: '大学英语四级词汇书', desc: '乱序版，背了一遍，书角有点卷，内页干净无标记。', price: 1200, cat: 'textbook' as const, cond: 'visibly_used' as const },
  { title: '数据结构（C语言版）', desc: '经典教材，保护得很好，包了书皮，内页无划痕。', price: 2800, cat: 'textbook' as const, cond: 'like_new' as const },
  { title: '线性代数及其应用', desc: '几乎全新，只用了一学期，课后习题有部分标记。', price: 2200, cat: 'textbook' as const, cond: 'like_new' as const },
  { title: '全新未拆封大学物理实验教程', desc: '多买了一本，全新未拆塑封，原价45元。', price: 3000, cat: 'textbook' as const, cond: 'brand_new' as const },
  { title: '二手iPad Air 4 64G', desc: '2022年购入，屏幕无划痕，电池健康度87%，带原装充电器和保护壳。可小刀。', price: 180000, cat: 'digital' as const, cond: 'slightly_used' as const },
  { title: '罗技K380蓝牙键盘', desc: '搭配iPad使用，粉色款，键盘无磨损，送两节备用电池。', price: 6500, cat: 'digital' as const, cond: 'like_new' as const },
  { title: 'AirPods Pro 第二代', desc: '2024年购买在保，使用不超过10次，带AppleCare+。', price: 120000, cat: 'digital' as const, cond: 'like_new' as const },
  { title: '小米手环8 NFC版', desc: '黑色，用了两个月，表带有轻微使用痕迹，功能一切正常。送充电线。', price: 8500, cat: 'digital' as const, cond: 'slightly_used' as const },
  { title: 'Kindle Paperwhite 4', desc: '日版32G漫画版，已刷多看系统，屏幕完好，续航正常。', price: 35000, cat: 'digital' as const, cond: 'visibly_used' as const },
  { title: '九成新台灯 LED护眼', desc: '三档调光，可折叠，宿舍用了一个学期，包装盒还在。', price: 2500, cat: 'lifestyle' as const, cond: 'like_new' as const },
  { title: '宿舍用小风扇 静音款', desc: 'USB供电，两档风速，静音效果好，夏天必备。', price: 1500, cat: 'lifestyle' as const, cond: 'slightly_used' as const },
  { title: '折叠床上桌', desc: '铝合金腿，带杯托和卡槽，几乎全新，买了发现用不上。', price: 3000, cat: 'lifestyle' as const, cond: 'like_new' as const },
  { title: '收纳箱 66L 三个装', desc: '搬家多买了三个，全新未拆，透明款带轮子。打包出，不单卖。', price: 4500, cat: 'lifestyle' as const, cond: 'brand_new' as const },
  { title: '无印良品风格加湿器', desc: '500ml容量，静音喷雾，带氛围灯，用了半个冬天。', price: 1800, cat: 'lifestyle' as const, cond: 'slightly_used' as const },
  { title: '尤尼克斯羽毛球拍 NR-750', desc: '只用了一学期体育课，拍框无磕碰，手胶新换的。送两个球。', price: 15000, cat: 'sports' as const, cond: 'like_new' as const },
  { title: '二手篮球 斯伯丁TF-500', desc: '室外打了几次，表皮有些磨损但不影响使用，送打气筒。', price: 4000, cat: 'sports' as const, cond: 'visibly_used' as const },
  { title: '瑜伽垫 加厚10mm', desc: '紫色，用了不到五次，展开没有异味。送绑带。', price: 2000, cat: 'sports' as const, cond: 'like_new' as const },
  { title: '跳绳 钢丝竞速款', desc: '轴承顺滑，手柄带计数功能，只用过一次。', price: 1200, cat: 'sports' as const, cond: 'brand_new' as const },
  { title: '俯卧撑支架 S型', desc: '钢制，防滑手柄，几乎全新，买重了出一对。', price: 800, cat: 'sports' as const, cond: 'like_new' as const },
  { title: '优衣库轻薄羽绒服 M码', desc: '黑色男款，只穿了两三次，洗过一次，没有变形。适合170-175。', price: 8000, cat: 'clothing' as const, cond: 'slightly_used' as const },
  { title: 'NIKE Air Force 1 43码', desc: '穿了不到一个月，鞋底磨损轻微，鞋面干净。原盒还在。', price: 25000, cat: 'clothing' as const, cond: 'slightly_used' as const },
  { title: '全新H&M格子衬衫 L码', desc: '吊牌未拆，买大了穿不了。原价149。', price: 5500, cat: 'clothing' as const, cond: 'brand_new' as const },
  { title: '学院风双肩包', desc: '灰色，容量大能装15.6寸笔记本，背了一个学期，拉链完好。', price: 3500, cat: 'clothing' as const, cond: 'visibly_used' as const },
  { title: '冬季围巾 羊毛混纺', desc: '卡其色，手感柔软，只戴过两次。送礼品袋。', price: 1800, cat: 'clothing' as const, cond: 'like_new' as const },
  { title: '校园纪念品 马克杯', desc: '校庆限定款，全新未使用，带包装盒。', price: 1500, cat: 'other' as const, cond: 'brand_new' as const },
  { title: '吉他调音器+变调夹套装', desc: '入门必备，调音器带背光，变调夹金属材质。打包出。', price: 2000, cat: 'other' as const, cond: 'like_new' as const },
  { title: '一次性胶片相机', desc: '买了三台自留一台，出两台全新未拆的，适合出门旅行拍。', price: 3500, cat: 'other' as const, cond: 'brand_new' as const },
  { title: '魔方 三阶竞速款', desc: '磁力版，手感顺滑，附赠教程卡片。', price: 1200, cat: 'other' as const, cond: 'slightly_used' as const },
  { title: '二手计算器 卡西欧FX-991CN', desc: '考试神器，中文版，功能完好，屏幕无划痕。', price: 4500, cat: 'other' as const, cond: 'slightly_used' as const },
  { title: '概率论与数理统计', desc: '浙大版，笔记较多但都是重点标注，适合复习用。', price: 1500, cat: 'textbook' as const, cond: 'visibly_used' as const },
  { title: '机械键盘 IKBC C87 茶轴', desc: '87键白色，键帽无打油，送了几个个性键帽。原包装盒还在。', price: 12000, cat: 'digital' as const, cond: 'slightly_used' as const },
]

const productImages = [
  '#4A90D9', '#7B68EE', '#52C41A', '#FAAD14', '#FF6B6B',
  '#45B7D1', '#F78FB3', '#786FA6', '#63CDDA', '#E77F67'
]

export const sellers: User[] = Array.from({ length: 8 }, (_, i) => randomSeller(i + 1))

export const products: Product[] = productTemplates.map((t, i) => ({
  id: `p_${String(i + 1).padStart(3, '0')}`,
  title: t.title,
  description: t.desc,
  images: Array.from({ length: rnd(1, 4) }, () => productImages[rnd(0, productImages.length - 1)]),
  price: t.price,
  condition: t.cond,
  category: t.cat,
  campus: campusList[rnd(0, campusList.length - 1)],
  status: i < 25 ? 'on_sale' : i < 28 ? 'pending_review' : i < 30 ? 'sold' : 'delisted',
  viewCount: rnd(10, 500),
  sellerId: sellers[rnd(0, sellers.length - 1)].id,
  createdAt: new Date(Date.now() - rnd(3600000, 1209600000)).toISOString(),
  updatedAt: new Date(Date.now() - rnd(3600000, 604800000)).toISOString()
}))

export const orders: Order[] = [
  {
    id: 'o_001', orderNo: '202605130001', productId: 'p_006', buyerId: 'u_001', sellerId: products[5].sellerId,
    amount: 180000, status: 'completed', createdAt: '2026-05-10T08:00:00Z', paidAt: '2026-05-10T08:05:00Z', completedAt: '2026-05-11T14:00:00Z'
  },
  {
    id: 'o_002', orderNo: '202605130002', productId: 'p_003', buyerId: 'u_001', sellerId: products[2].sellerId,
    amount: 2800, status: 'pending_payment', createdAt: new Date().toISOString(), expireAt: new Date(Date.now() + 1800000).toISOString()
  },
  {
    id: 'o_003', orderNo: '202605130003', productId: 'p_007', buyerId: 'u_001', sellerId: products[6].sellerId,
    amount: 6500, status: 'paid_escrow', createdAt: '2026-05-12T10:00:00Z', paidAt: '2026-05-12T10:03:00Z'
  },
  {
    id: 'o_004', orderNo: '202605130004', productId: 'p_011', buyerId: 'u_001', sellerId: products[10].sellerId,
    amount: 2500, status: 'refunding', createdAt: '2026-05-11T09:00:00Z', paidAt: '2026-05-11T09:02:00Z', refundReason: 'not_as_described', refundDesc: '实物颜色和描述不符，申请退货退款'
  },
  {
    id: 'o_005', orderNo: '202605130005', productId: 'p_015', buyerId: 'u_001', sellerId: products[14].sellerId,
    amount: 15000, status: 'refunded', createdAt: '2026-05-08T07:00:00Z', paidAt: '2026-05-08T07:04:00Z', completedAt: '2026-05-09T10:00:00Z', refundReason: 'other'
  },
  {
    id: 'o_006', orderNo: '202605130006', productId: 'p_001', buyerId: sellers[0].id, sellerId: 'u_001',
    amount: 3500, status: 'paid_escrow', createdAt: '2026-05-13T06:00:00Z', paidAt: '2026-05-13T06:02:00Z'
  },
  {
    id: 'o_007', orderNo: '202605130007', productId: 'p_002', buyerId: sellers[1].id, sellerId: 'u_001',
    amount: 1200, status: 'completed', createdAt: '2026-05-05T08:00:00Z', paidAt: '2026-05-05T08:05:00Z', completedAt: '2026-05-06T12:00:00Z'
  },
  {
    id: 'o_008', orderNo: '202605130008', productId: 'p_004', buyerId: sellers[2].id, sellerId: 'u_001',
    amount: 2200, status: 'completed', createdAt: '2026-05-01T10:00:00Z', paidAt: '2026-05-01T10:03:00Z', completedAt: '2026-05-03T15:00:00Z'
  },
]

export const conversations: Conversation[] = [
  { id: 'conv_001', productId: 'p_006', participantId: products[5].sellerId, unreadCount: 3, lastMessage: '好的，明天下午3点在图书馆门口见吧', lastMessageTime: '2026-05-13T07:55:00Z', updatedAt: '2026-05-13T07:55:00Z' },
  { id: 'conv_002', productId: 'p_003', participantId: products[2].sellerId, unreadCount: 0, lastMessage: '书还在吗？', lastMessageTime: '2026-05-13T06:30:00Z', updatedAt: '2026-05-13T06:30:00Z' },
  { id: 'conv_003', productId: 'p_007', participantId: products[6].sellerId, unreadCount: 1, lastMessage: '最低80可以吗？', lastMessageTime: '2026-05-12T22:00:00Z', updatedAt: '2026-05-12T22:00:00Z' },
  { id: 'conv_004', productId: 'p_011', participantId: products[10].sellerId, unreadCount: 0, lastMessage: '好的，谢谢理解', lastMessageTime: '2026-05-11T10:00:00Z', updatedAt: '2026-05-11T10:00:00Z' },
  { id: 'conv_005', productId: 'p_001', participantId: sellers[0].id, unreadCount: 2, lastMessage: '你好，这本书可以便宜点吗？', lastMessageTime: '2026-05-13T08:10:00Z', updatedAt: '2026-05-13T08:10:00Z' },
]

export const messages: Record<string, Message[]> = {
  'conv_001': [
    { id: 'msg_001', conversationId: 'conv_001', senderId: 'u_001', content: '你好，iPad还在吗？', createdAt: '2026-05-13T07:00:00Z' },
    { id: 'msg_002', conversationId: 'conv_001', senderId: products[5].sellerId, content: '在的，电池健康87%，平时都带壳使用', createdAt: '2026-05-13T07:05:00Z' },
    { id: 'msg_003', conversationId: 'conv_001', senderId: 'u_001', content: '可以小刀吗？1800有点贵了', createdAt: '2026-05-13T07:06:00Z' },
    { id: 'msg_004', conversationId: 'conv_001', senderId: products[5].sellerId, content: '最低1700，带壳和充电器一起。当面交易你也能验机', createdAt: '2026-05-13T07:10:00Z' },
    { id: 'msg_005', conversationId: 'conv_001', senderId: 'u_001', content: '好的那明天方便面交吗？我在本部校区', createdAt: '2026-05-13T07:30:00Z' },
    { id: 'msg_006', conversationId: 'conv_001', senderId: products[5].sellerId, content: '可以，我也是本部的。明天下午有空吗？', createdAt: '2026-05-13T07:40:00Z' },
    { id: 'msg_007', conversationId: 'conv_001', senderId: 'u_001', content: '下午3点后都可以', createdAt: '2026-05-13T07:50:00Z' },
    { id: 'msg_008', conversationId: 'conv_001', senderId: products[5].sellerId, content: '好的，明天下午3点在图书馆门口见吧', createdAt: '2026-05-13T07:55:00Z' },
  ],
  'conv_002': [
    { id: 'msg_101', conversationId: 'conv_002', senderId: 'u_001', content: '你好，数据结构那本书还在吗？', createdAt: '2026-05-13T06:00:00Z' },
    { id: 'msg_102', conversationId: 'conv_002', senderId: products[2].sellerId, content: '在的，书保护得很好，包了书皮', createdAt: '2026-05-13T06:20:00Z' },
    { id: 'msg_103', conversationId: 'conv_002', senderId: 'u_001', content: '书还在吗？', createdAt: '2026-05-13T06:30:00Z' },
  ],
  'conv_003': [
    { id: 'msg_201', conversationId: 'conv_003', senderId: 'u_001', content: '键盘出了吗？', createdAt: '2026-05-12T20:00:00Z' },
    { id: 'msg_202', conversationId: 'conv_003', senderId: products[6].sellerId, content: '还没，粉色K380，几乎全新', createdAt: '2026-05-12T20:30:00Z' },
    { id: 'msg_203', conversationId: 'conv_003', senderId: 'u_001', content: '65有点贵了，80可以么', createdAt: '2026-05-12T21:00:00Z' },
    { id: 'msg_204', conversationId: 'conv_003', senderId: products[6].sellerId, content: '最低90哦，这个真的几乎全新，原价199的', createdAt: '2026-05-12T21:30:00Z' },
    { id: 'msg_205', conversationId: 'conv_003', senderId: 'u_001', content: '最低80可以吗？', createdAt: '2026-05-12T22:00:00Z' },
  ],
  'conv_004': [
    { id: 'msg_301', conversationId: 'conv_004', senderId: 'u_001', content: '台灯还在吗？三档调光的那款', createdAt: '2026-05-11T08:00:00Z' },
    { id: 'msg_302', conversationId: 'conv_004', senderId: products[10].sellerId, content: '在的，包装盒都还在', createdAt: '2026-05-11T08:30:00Z' },
    { id: 'msg_303', conversationId: 'conv_004', senderId: 'u_001', content: '那我直接拍了', createdAt: '2026-05-11T09:00:00Z' },
    { id: 'msg_304', conversationId: 'conv_004', senderId: products[10].sellerId, content: '好的，谢谢理解', createdAt: '2026-05-11T10:00:00Z' },
  ],
  'conv_005': [
    { id: 'msg_401', conversationId: 'conv_005', senderId: sellers[0].id, content: '你好，高等数学两本打包可以便宜吗？', createdAt: '2026-05-13T08:00:00Z' },
    { id: 'msg_402', conversationId: 'conv_005', senderId: 'u_001', content: '可以，两本一起30块', createdAt: '2026-05-13T08:05:00Z' },
    { id: 'msg_403', conversationId: 'conv_005', senderId: sellers[0].id, content: '好的，那我直接下单了', createdAt: '2026-05-13T08:08:00Z' },
    { id: 'msg_404', conversationId: 'conv_005', senderId: 'u_001', content: 'ok，面交还是？你在哪个校区', createdAt: '2026-05-13T08:09:00Z' },
    { id: 'msg_405', conversationId: 'conv_005', senderId: sellers[0].id, content: '你好，这本书可以便宜点吗？', createdAt: '2026-05-13T08:10:00Z' },
  ],
}

export const reviews: Review[] = [
  { id: 'r_001', orderId: 'o_001', reviewerId: 'u_001', revieweeId: products[5].sellerId, productId: 'p_006', rating: 5, content: 'iPad成色很好，卖家很耐心当面验机，非常满意！', tags: ['沟通顺畅', '商品如实'], isPublic: true, createdAt: '2026-05-11T15:00:00Z' },
  { id: 'r_002', orderId: 'o_001', reviewerId: products[5].sellerId, revieweeId: 'u_001', productId: 'p_006', rating: 5, content: '买家很爽快，当面交易很顺利', tags: ['沟通顺畅'], isPublic: true, createdAt: '2026-05-11T15:30:00Z' },
  { id: 'r_003', orderId: 'o_007', reviewerId: sellers[1].id, revieweeId: 'u_001', productId: 'p_002', rating: 4, content: '书收到了，内容完整，就是快递包装可以再好一点', tags: ['商品如实'], isPublic: true, createdAt: '2026-05-07T10:00:00Z' },
  { id: 'r_004', orderId: 'o_007', reviewerId: 'u_001', revieweeId: sellers[1].id, productId: 'p_002', rating: 5, content: '沟通很顺畅，买家付款很快', tags: ['沟通顺畅', '付款及时'], isPublic: true, createdAt: '2026-05-07T11:00:00Z' },
  { id: 'r_005', orderId: 'o_008', reviewerId: sellers[2].id, revieweeId: 'u_001', productId: 'p_004', rating: 5, content: '书很新，物超所值！', tags: ['商品如实', '物超所值'], isPublic: true, createdAt: '2026-05-04T10:00:00Z' },
  { id: 'r_006', orderId: 'o_008', reviewerId: 'u_001', revieweeId: sellers[2].id, productId: 'p_004', rating: 4, content: '不错的买家，交易顺利', tags: ['沟通顺畅'], isPublic: true, createdAt: '2026-05-04T14:00:00Z' },
  { id: 'r_007', orderId: 'o_999', reviewerId: sellers[3].id, revieweeId: 'u_001', productId: 'p_010', rating: 5, content: '卖家回复及时，东西和描述一样', tags: ['沟通顺畅', '商品如实'], isPublic: true, createdAt: '2026-04-20T09:00:00Z' },
  { id: 'r_008', orderId: 'o_998', reviewerId: sellers[4].id, revieweeId: 'u_001', productId: 'p_016', rating: 5, content: '非常负责，还主动提醒确认收货', tags: ['沟通顺畅', '认真负责'], isPublic: true, createdAt: '2026-04-15T10:00:00Z' },
  { id: 'r_009', orderId: 'o_997', reviewerId: sellers[5].id, revieweeId: 'u_001', productId: 'p_020', rating: 3, content: '商品能用，但有些小毛病没有提前说明', tags: [], isPublic: true, createdAt: '2026-04-10T08:00:00Z' },
  { id: 'r_010', orderId: 'o_996', reviewerId: sellers[0].id, revieweeId: 'u_001', productId: 'p_025', rating: 5, content: '非常愉快的交易体验！', tags: ['沟通顺畅', '商品如实', '物超所值'], isPublic: true, createdAt: '2026-04-05T12:00:00Z' },
]

export const disputes: Dispute[] = [
  {
    id: 'd_001', orderId: 'o_004', creatorId: 'u_001', type: 'product_issue',
    description: '购买的台灯颜色与商品详情页描述严重不符，详情写的是白色，收到是米黄色。已经和卖家沟通过，卖家不承认。请求平台介入处理。',
    status: 'processing',
    comments: [
      { id: 'dc_001', senderId: 'u_001', content: '台灯颜色是米黄色不是白色，描述里写的白色', createdAt: '2026-05-11T10:00:00Z' },
      { id: 'dc_002', senderId: products[10].sellerId, content: '这个本来就是暖白色，照片里也能看出来', createdAt: '2026-05-11T11:00:00Z' },
      { id: 'dc_003', senderId: 'u_001', content: '你在描述里写的是白色，没有说暖白色。而且照片拍出来和实物差距很大', createdAt: '2026-05-11T12:00:00Z' },
    ],
    createdAt: '2026-05-11T09:30:00Z'
  },
  {
    id: 'd_002', orderId: 'o_005', creatorId: 'u_001', type: 'payment_issue',
    description: '已经申请退款并获得卖家同意，但退款金额一直没有到账，已经超过3天了。',
    status: 'resolved',
    comments: [
      { id: 'dc_101', senderId: 'u_001', content: '退款已经3天了还没到账', createdAt: '2026-05-10T08:00:00Z' },
      { id: 'dc_102', senderId: 'admin_001', content: '经核实，退款已经处理完毕，请查看微信零钱明细。如有问题可以继续反馈。', createdAt: '2026-05-10T15:00:00Z' },
    ],
    createdAt: '2026-05-09T20:00:00Z'
  },
]

export function getProduct(id: string) { return products.find(p => p.id === id) }
export function getUser(id: string) { return [currentUser, ...sellers].find(u => u.id === id) || currentUser }
export function getOrder(id: string) { return orders.find(o => o.id === id) }
export function getConversation(id: string) { return conversations.find(c => c.id === id) }
export function getReviewsByUser(userId: string) { return reviews.filter(r => r.revieweeId === userId || r.reviewerId === userId) }
export function getReviewsForUser(userId: string) { return reviews.filter(r => r.revieweeId === userId) }
export function getReviewsByUser_given(userId: string) { return reviews.filter(r => r.reviewerId === userId) }

export function maskStudentId(id: string) {
  if (id.length <= 4) return id
  return id.substring(0, 2) + '****' + id.substring(id.length - 2)
}

export function maskEmail(email: string) {
  const [name, domain] = email.split('@')
  if (name.length <= 1) return email
  return name[0] + '****' + '@' + domain
}

export function maskPhone(phone: string) {
  if (phone.length !== 11) return phone
  return phone.substring(0, 3) + '****' + phone.substring(7)
}

export function getConditionLabel(c: string) { return conditionLabels[c] || c }
export function getCategoryLabel(c: string) { return categoryLabels[c] || c }
export function getStatusLabel(s: string): string {
  const map: Record<string, string> = {
    on_sale: '在售', pending_review: '审核中', sold: '已售出', delisted: '已下架',
    pending_payment: '待付款', paid_escrow: '已付款', completed: '已完成',
    refunding: '退款中', refunded: '已退款', cancelled: '已取消',
    pending: '待受理', processing: '处理中', resolved: '已解决', rejected: '已驳回'
  }
  return map[s] || s
}

export function relativeTime(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime()
  const m = Math.floor(diff / 60000)
  if (m < 1) return '刚刚'
  if (m < 60) return `${m}分钟前`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}小时前`
  const d = Math.floor(h / 24)
  if (d < 30) return `${d}天前`
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

export function formatPrice(cents: number): string {
  return '¥' + (cents / 100).toFixed(2)
}

export { campusList }

export function addProduct(product: Product) {
  product.id = `p_${String(products.length + 1).padStart(3, '0')}`
  products.unshift(product)
}

export function updateProductStatus(id: string, status: Product['status']) {
  const p = products.find(p => p.id === id)
  if (p) { p.status = status; p.updatedAt = new Date().toISOString() }
}

export function simulateMessage(convId: string) {
  const msgs = messages[convId] || []
  const autoReplies = [
    '好的，没问题~', '在的，随时可以面交', '可以的，你拍吧', '嗯嗯，那到时候联系',
    '哈哈好的', 'ok收到了', '没问题！', '行，那就这样'
  ]
  const newMsg: Message = {
    id: `msg_auto_${Date.now()}`,
    conversationId: convId,
    senderId: getConversation(convId)?.participantId || '',
    content: autoReplies[Math.floor(Math.random() * autoReplies.length)],
    createdAt: new Date().toISOString()
  }
  if (!messages[convId]) messages[convId] = []
  messages[convId].push(newMsg)
  return newMsg
}