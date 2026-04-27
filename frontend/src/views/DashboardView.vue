<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { MessageOutlined, ReadOutlined, RobotOutlined } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { getAuthUser, clearAuthState } from '../auth/auth';
import { logout } from '../api/auth';
import { getDashboardStats, type DashboardStats } from '../api/dashboard';

const router = useRouter();
const loading = ref(true);
const authUser = getAuthUser();
const stats = ref<DashboardStats>({
  knowledgeBaseCount: 0,
  sessionCount: 0,
  recentMessages: [],
});

onMounted(async () => {
  stats.value = await getDashboardStats();
  loading.value = false;
});

const summaryCards = computed(() => [
  { title: '知识库数量', value: stats.value.knowledgeBaseCount, icon: ReadOutlined },
  { title: '会话数量', value: stats.value.sessionCount, icon: MessageOutlined },
  { title: 'Copilot 状态', value: '在线', icon: RobotOutlined },
]);

async function handleLogout() {
  try {
    await logout();
  } finally {
    clearAuthState();
    router.push('/login');
  }
}
</script>

<template>
  <main class="page-shell">
    <section class="dashboard-header">
      <div>
        <p class="section-kicker">Overview</p>
        <h1>运维 Copilot 控制台</h1>
        <p class="section-copy">
          {{ authUser?.nickname || authUser?.username || '当前用户' }}，当前阶段先提供基础骨架，后续可继续接入知识库管理、会话检索和运行状态面板。
        </p>
      </div>
      <div class="dashboard-actions">
        <a-button @click="handleLogout">退出登录</a-button>
        <a-button type="primary" @click="$router.push('/chat')">进入聊天页</a-button>
      </div>
    </section>

    <section class="dashboard-grid">
      <a-card class="intro-card" title="项目介绍" :bordered="false">
        <p>Spring AI Ops Copilot 用于把告警、日志、知识库和会话整合到一套运维协作界面里。</p>
        <p>当前前端已接入 Vue 3、TypeScript、Ant Design Vue、Vue Router 和 axios 封装。</p>
      </a-card>

      <div class="stats-grid">
        <a-card v-for="card in summaryCards" :key="card.title" class="stat-card" :bordered="false" :loading="loading">
          <template #title>
            <span class="stat-title">
              <component :is="card.icon" />
              {{ card.title }}
            </span>
          </template>
          <div class="stat-value">{{ card.value }}</div>
        </a-card>
      </div>

      <a-card class="messages-card" title="最近消息" :bordered="false" :loading="loading">
        <a-list :data-source="stats.recentMessages">
          <template #renderItem="{ item }">
            <a-list-item class="recent-message-item" @click="$router.push(`/chat?sessionId=${item.sessionId}`)">
              <a-list-item-meta :title="item.title" :description="item.preview" />
              <span class="message-time">{{ item.time }}</span>
            </a-list-item>
          </template>
        </a-list>
      </a-card>
    </section>
  </main>
</template>
