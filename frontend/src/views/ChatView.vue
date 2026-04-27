<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { RobotOutlined, SearchOutlined, SendOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import DOMPurify from 'dompurify';
import { marked } from 'marked';
import { useRoute, useRouter } from 'vue-router';
import { clearAuthState, getAuthUser } from '../auth/auth';
import { logout } from '../api/auth';
import {
  getChatSessionMessages,
  getChatSessions,
  searchChatMessages,
  sendChatMessage,
  type ChatMessageSearchResult,
} from '../api/chat';

interface SessionItem {
  id: string;
  title: string;
  updatedAt: string;
}

interface ChatItem {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  time: string;
}

const loading = ref(false);
const sessionListLoading = ref(false);
const route = useRoute();
const router = useRouter();
const authUser = getAuthUser();
const sessionId = ref<string>('');
const sessionKeyword = ref('');
const input = ref('');
const sessions = ref<SessionItem[]>([]);
const messages = ref<ChatItem[]>([]);
const chatContentRef = ref<HTMLDivElement | null>(null);
const searchOpen = ref(false);
const searchKeyword = ref('');
const searchLoading = ref(false);
const searchResults = ref<ChatMessageSearchResult[]>([]);
const activeSearchMessageId = ref('');

const canSend = computed(() => input.value.trim().length > 0 && !loading.value);
const filteredSessions = computed(() => {
  const keyword = sessionKeyword.value.trim().toLowerCase();
  if (!keyword) {
    return sessions.value;
  }

  return sessions.value.filter((item) => item.title.toLowerCase().includes(keyword));
});
const currentSessionTitle = computed(() => {
  if (!sessionId.value) {
    return '新会话';
  }

  return sessions.value.find((item) => item.id === sessionId.value)?.title || '当前会话';
});

marked.setOptions({
  breaks: true,
  gfm: true,
});

function scrollToLatestMessage() {
  void nextTick(() => {
    const container = chatContentRef.value;
    if (!container) {
      return;
    }

    container.scrollTop = container.scrollHeight;
  });
}

function scrollToMatchedMessage(messageId: string) {
  void nextTick(() => {
    const container = chatContentRef.value;
    const target = container?.querySelector<HTMLElement>(`[data-message-id="${messageId}"]`);
    if (!container || !target) {
      return;
    }

    const top = target.offsetTop - container.clientHeight / 2 + target.clientHeight / 2;
    container.scrollTo({
      top: Math.max(0, top),
      behavior: 'smooth',
    });
  });
}

async function selectSession(id: string, focusMessageId?: string) {
  sessionId.value = id;
  activeSearchMessageId.value = focusMessageId || '';
  await router.replace({ path: '/chat', query: { sessionId: id } });
  await loadSessionMessages(id, focusMessageId);
}

function handleCreateSession() {
  sessionId.value = '';
  sessionKeyword.value = '';
  activeSearchMessageId.value = '';
  messages.value = [
    {
      id: 'new-session-welcome',
      role: 'assistant',
      content: '新会话已创建。你可以直接输入运维问题、日志片段或变更需求。',
      time: dayjs().format('HH:mm'),
    },
  ];
  scrollToLatestMessage();
  void router.replace('/chat');
}

async function loadSessions(preferredSessionId?: string) {
  sessionListLoading.value = true;
  try {
    const response = await getChatSessions();
    sessions.value = response.map((item) => ({
      id: item.sessionId,
      title: item.title,
      updatedAt: dayjs(item.updatedAt).format('MM-DD HH:mm'),
    }));

    const targetSessionId = preferredSessionId || route.query.sessionId?.toString() || sessions.value[0]?.id;
    if (targetSessionId) {
      await selectSession(targetSessionId);
    } else {
      sessionId.value = '';
      messages.value = [
        {
          id: 'default-welcome',
          role: 'assistant',
          content: '我是 Ops Copilot。你可以直接输入告警、日志片段或变更需求。',
          time: dayjs().format('HH:mm'),
        },
      ];
      scrollToLatestMessage();
    }
  } finally {
    sessionListLoading.value = false;
  }
}

async function loadSessionMessages(targetSessionId: string, focusMessageId?: string) {
  const detail = await getChatSessionMessages(targetSessionId);
  messages.value = detail.messages.length
    ? detail.messages.map((item) => ({
        id: item.id,
        role: item.role,
        content: item.content,
        time: dayjs(item.createdAt).format('HH:mm'),
      }))
    : [
        {
          id: `empty-${targetSessionId}`,
          role: 'assistant',
          content: '当前会话还没有历史消息，可以直接开始提问。',
          time: dayjs().format('HH:mm'),
        },
      ];
  if (focusMessageId) {
    activeSearchMessageId.value = focusMessageId;
    scrollToMatchedMessage(focusMessageId);
  } else {
    activeSearchMessageId.value = '';
    scrollToLatestMessage();
  }
}

async function handleLogout() {
  try {
    await logout();
  } finally {
    clearAuthState();
    router.push('/login');
  }
}

async function handleSend() {
  if (!canSend.value) {
    return;
  }

  const content = input.value.trim();
  messages.value.push({
    id: `local-user-${Date.now()}`,
    role: 'user',
    content,
    time: dayjs().format('HH:mm'),
  });
  input.value = '';
  activeSearchMessageId.value = '';
  scrollToLatestMessage();
  loading.value = true;

  try {
    const response = await sendChatMessage({
      message: content,
      sessionId: sessionId.value || undefined,
    });

    sessionId.value = response.sessionId;
    await loadSessions(response.sessionId);
  } finally {
    loading.value = false;
  }
}

function renderMarkdown(content: string) {
  const rawHtml = marked.parse(content) as string;
  return DOMPurify.sanitize(rawHtml);
}

function escapeHtml(value: string) {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function escapeRegExp(value: string) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function renderHighlightedSnippet(content: string) {
  const keyword = searchKeyword.value.trim();
  const safeContent = escapeHtml(content);
  if (!keyword) {
    return safeContent;
  }

  const pattern = new RegExp(`(${escapeRegExp(keyword)})`, 'ig');
  return safeContent.replace(pattern, '<mark>$1</mark>');
}

function openSearchDialog() {
  searchOpen.value = true;
}

async function handleSearchMessages(value?: string) {
  const keyword = (value ?? searchKeyword.value).trim();
  searchKeyword.value = keyword;
  if (!keyword) {
    searchResults.value = [];
    return;
  }

  searchLoading.value = true;
  try {
    searchResults.value = await searchChatMessages(keyword);
  } finally {
    searchLoading.value = false;
  }
}

async function handleSelectSearchResult(item: ChatMessageSearchResult) {
  searchOpen.value = false;
  await selectSession(item.sessionId, item.messageId);
}

watch(
  () => route.query.sessionId,
  async (newValue) => {
    const targetSessionId = newValue?.toString();
    if (targetSessionId && targetSessionId !== sessionId.value) {
      await selectSession(targetSessionId);
    }
  },
);

onMounted(async () => {
  await loadSessions();
});
</script>

<template>
  <main class="chat-shell">
    <aside class="chat-sidebar">
      <div class="sidebar-top">
        <div class="sidebar-brand">
          <strong>Ops Copilot</strong>
          <small>{{ sessions.length }} 个会话</small>
        </div>
        <a-button type="primary" block class="session-create-btn" @click="handleCreateSession">新建会话</a-button>
      </div>

      <div class="session-toolbar">
        <a-input
          v-model:value="sessionKeyword"
          class="session-search"
          placeholder="搜索会话标题"
          allow-clear
        />
      </div>

      <a-list :data-source="filteredSessions" class="session-list" :loading="sessionListLoading">
        <template #renderItem="{ item }">
          <a-list-item class="session-item" :class="{ active: item.id === sessionId }" @click="selectSession(item.id)">
            <div>
              <strong>{{ item.title }}</strong>
              <small>{{ item.updatedAt }}</small>
            </div>
          </a-list-item>
        </template>
      </a-list>

      <div class="sidebar-footer">
        <div class="sidebar-user">
          <strong>{{ authUser?.nickname || authUser?.username || '当前用户' }}</strong>
          <small>已登录</small>
        </div>
        <div class="sidebar-footer-actions">
          <a-button @click="$router.push('/dashboard')">首页</a-button>
          <a-button @click="handleLogout">退出</a-button>
        </div>
      </div>
    </aside>

    <section class="chat-main">
      <header class="chat-header">
        <div class="chat-title">
          <span class="bot-mark"><RobotOutlined /></span>
          <div class="chat-title-copy">
            <strong>{{ currentSessionTitle }}</strong>
            <small>{{ sessionId || 'new session' }}</small>
          </div>
        </div>
        <div class="chat-header-actions">
          <a-button shape="circle" @click="openSearchDialog">
            <template #icon><SearchOutlined /></template>
          </a-button>
        </div>
      </header>

      <div ref="chatContentRef" class="chat-content">
        <div class="chat-thread">
          <article
            v-for="(item, index) in messages"
            :key="item.id || index"
            :data-message-id="item.id"
            :class="['message', item.role, { 'is-search-hit': item.id === activeSearchMessageId }]"
          >
            <span class="time">{{ item.time }}</span>
            <div class="message-body markdown-body" v-html="renderMarkdown(item.content)"></div>
          </article>
          <a-skeleton v-if="loading" active :paragraph="{ rows: 2 }" />
        </div>
      </div>

      <footer class="chat-composer">
        <div class="chat-composer-wrap">
          <div class="chat-composer-field">
            <a-textarea
              v-model:value="input"
              :auto-size="{ minRows: 2, maxRows: 6 }"
              placeholder="给 Ops Copilot 发送消息"
              @press-enter.prevent="handleSend"
            />
            <div class="chat-composer-meta">
              <span class="composer-tip">支持 Markdown，Shift + Enter 可换行</span>
              <a-button type="primary" size="large" :disabled="!canSend" :loading="loading" @click="handleSend">
                <template #icon><SendOutlined /></template>
                发送
              </a-button>
            </div>
          </div>
        </div>
      </footer>
    </section>

    <a-modal
      v-model:open="searchOpen"
      title="搜索聊天内容"
      :footer="null"
      :width="720"
      destroy-on-close
    >
      <div class="chat-search-modal">
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="输入关键词，模糊匹配历史聊天内容"
          size="large"
          enter-button="搜索"
          allow-clear
          :loading="searchLoading"
          @search="handleSearchMessages"
        />

        <a-list
          class="chat-search-result-list"
          :data-source="searchResults"
          :loading="searchLoading"
          :locale="{ emptyText: searchKeyword ? '没有找到匹配内容' : '输入关键词开始搜索' }"
        >
          <template #renderItem="{ item }">
            <a-list-item class="chat-search-result-item" @click="handleSelectSearchResult(item)">
              <div class="chat-search-result-head">
                <strong>{{ item.sessionTitle }}</strong>
                <small>{{ dayjs(item.createdAt).format('MM-DD HH:mm') }}</small>
              </div>
              <p v-html="renderHighlightedSnippet(item.snippet)"></p>
            </a-list-item>
          </template>
        </a-list>
      </div>
    </a-modal>
  </main>
</template>
