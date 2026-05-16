<template>
  <div class="messages-page">
    <div class="conv-list-panel">
      <h3 class="panel-title">💬 会话列表</h3>
      <div v-if="conversations.length" class="conv-list">
        <div v-for="conv in conversations" :key="conv.id" class="conv-item" :class="{ active: activeConv === conv.id }" @click="selectConv(conv.id)">
          <div class="conv-avatar">{{ getParticipant(conv)?.nickname?.charAt(0) }}</div>
          <div class="conv-info">
            <div class="conv-top">
              <span class="conv-name">{{ getParticipant(conv)?.nickname }}</span>
              <span class="conv-time">{{ relativeTime(conv.lastMessageTime) }}</span>
            </div>
            <div class="conv-bottom">
              <span class="conv-preview">{{ conv.lastMessage }}</span>
              <span v-if="conv.unreadCount > 0" class="conv-badge">{{ conv.unreadCount }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state"><span class="icon">💬</span><p>暂无会话</p></div>
    </div>

    <div class="chat-panel">
      <template v-if="activeConvData">
        <div class="chat-header">
          <div class="chat-avatar">{{ getParticipant(activeConvData)?.nickname?.charAt(0) }}</div>
          <div>
            <strong>{{ getParticipant(activeConvData)?.nickname }}</strong>
            <p class="chat-product" @click="goProduct(activeConvData.productId)">📦 {{ getProduct(activeConvData.productId)?.title }}</p>
          </div>
        </div>
        <div class="chat-messages" ref="msgContainer">
          <div v-for="msg in currentMessages" :key="msg.id" class="msg-wrapper">
            <div v-if="msg.senderId === currentUser.id" class="msg-self">
              <div class="msg-bubble self">{{ msg.content }}</div>
            </div>
            <div v-else class="msg-other">
              <div class="msg-avatar">{{ getUser(msg.senderId)?.nickname?.charAt(0) }}</div>
              <div class="msg-bubble other">{{ msg.content }}</div>
            </div>
          </div>
        </div>
        <div class="chat-input-area">
          <input v-model="newMsg" placeholder="输入消息..." @keyup.enter="sendMsg" />
          <button class="btn btn-primary" @click="sendMsg" :disabled="!newMsg.trim()">发送</button>
        </div>
      </template>
      <div v-else class="empty-state" style="height:100%"><span class="icon">💬</span><p>选择一个会话开始聊天</p></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { conversations, messages, currentUser, getConversation, getUser, getProduct, relativeTime, simulateMessage } from '@/mock/data'

const router = useRouter()
const activeConv = ref('')
const newMsg = ref('')
const msgContainer = ref<HTMLElement | null>(null)

const activeConvData = computed(() => activeConv.value ? getConversation(activeConv.value) : null)
const currentMessages = computed(() => activeConv.value ? (messages[activeConv.value] || []) : [])

function getParticipant(conv: any) { return getUser(conv.participantId) }

function selectConv(id: string) {
  activeConv.value = id
  const conv = getConversation(id)
  if (conv) conv.unreadCount = 0
  nextTick(scrollBottom)
}

function scrollBottom() {
  nextTick(() => {
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  })
}

function sendMsg() {
  if (!newMsg.value.trim() || !activeConv.value) return
  const msg = {
    id: `msg_${Date.now()}`,
    conversationId: activeConv.value,
    senderId: currentUser.id,
    content: newMsg.value.trim(),
    createdAt: new Date().toISOString()
  }
  if (!messages[activeConv.value]) messages[activeConv.value] = []
  messages[activeConv.value].push(msg)
  const conv = getConversation(activeConv.value)
  if (conv) { conv.lastMessage = msg.content; conv.lastMessageTime = msg.createdAt; conv.updatedAt = msg.createdAt }
  newMsg.value = ''
  scrollBottom()

  setTimeout(() => {
    if (activeConv.value) {
      const reply = simulateMessage(activeConv.value)
      const c = getConversation(activeConv.value)
      if (c && reply) { c.lastMessage = reply.content; c.lastMessageTime = reply.createdAt; c.unreadCount++ }
      scrollBottom()
    }
  }, 1000)
}

function goProduct(id: string) { router.push(`/product/${id}`) }

watch(activeConv, () => scrollBottom())
</script>

<style scoped>
.messages-page { display: flex; height: calc(100vh - var(--header-h) - 48px); gap: 0; border-radius: var(--radius); overflow: hidden; box-shadow: var(--shadow); }
.conv-list-panel { width: 300px; background: var(--bg-card); border-right: 1px solid var(--border); display: flex; flex-direction: column; }
.panel-title { padding: 16px; font-size: 16px; border-bottom: 1px solid var(--border-light); }
.conv-list { flex: 1; overflow-y: auto; }
.conv-item { display: flex; gap: 10px; padding: 14px 16px; cursor: pointer; transition: background var(--transition); border-bottom: 1px solid var(--border-light); }
.conv-item:hover, .conv-item.active { background: rgba(74,144,217,0.06); }
.conv-avatar { width: 40px; height: 40px; border-radius: 50%; background: var(--primary-light); color: white; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 600; flex-shrink: 0; }
.conv-info { flex: 1; min-width: 0; }
.conv-top { display: flex; justify-content: space-between; margin-bottom: 4px; }
.conv-name { font-size: 14px; font-weight: 500; }
.conv-time { font-size: 11px; color: var(--text-muted); }
.conv-bottom { display: flex; justify-content: space-between; align-items: center; }
.conv-preview { font-size: 12px; color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 180px; }
.conv-badge { background: var(--danger); color: white; font-size: 11px; min-width: 18px; height: 18px; border-radius: 9px; display: flex; align-items: center; justify-content: center; padding: 0 4px; }
.chat-panel { flex: 1; display: flex; flex-direction: column; background: var(--bg-card); }
.chat-header { display: flex; align-items: center; gap: 10px; padding: 12px 16px; border-bottom: 1px solid var(--border-light); }
.chat-avatar { width: 36px; height: 36px; border-radius: 50%; background: var(--primary-light); color: white; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 600; }
.chat-product { font-size: 12px; color: var(--primary); cursor: pointer; }
.chat-messages { flex: 1; overflow-y: auto; padding: 16px; background: var(--bg-main); }
.msg-wrapper { margin-bottom: 12px; }
.msg-self { display: flex; justify-content: flex-end; }
.msg-other { display: flex; gap: 8px; }
.msg-avatar { width: 30px; height: 30px; border-radius: 50%; background: var(--bg-main); display: flex; align-items: center; justify-content: center; font-size: 12px; flex-shrink: 0; color: var(--text-muted); }
.msg-bubble { max-width: 65%; padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.5; word-break: break-word; }
.msg-bubble.self { background: var(--primary); color: white; border-bottom-right-radius: 4px; }
.msg-bubble.other { background: white; border-bottom-left-radius: 4px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
.chat-input-area { display: flex; gap: 10px; padding: 12px 16px; border-top: 1px solid var(--border-light); }
.chat-input-area input { flex: 1; padding: 10px 14px; border: 1px solid var(--border); border-radius: 20px; font-size: 14px; }
</style>