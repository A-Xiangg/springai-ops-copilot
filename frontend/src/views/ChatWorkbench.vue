<script setup lang="ts">
import { computed, ref } from 'vue';
import { RobotOutlined, SendOutlined, ThunderboltOutlined } from '@ant-design/icons-vue';
import { message as antMessage } from 'ant-design-vue';
import dayjs from 'dayjs';
import { sendChatMessage } from '../api/chat';

interface ChatItem {
  role: 'user' | 'assistant';
  content: string;
  time: string;
}

const sessionId = ref<string>();
const input = ref('帮我排查 PostgreSQL 连接池耗尽的问题');
const loading = ref(false);
const messages = ref<ChatItem[]>([
  {
    role: 'assistant',
    content: '我是 Ops Copilot。可以帮你分析告警、生成排查步骤、整理变更风险和沉淀运维知识。',
    time: dayjs().format('HH:mm'),
  },
]);

const canSend = computed(() => input.value.trim().length > 0 && !loading.value);

async function handleSend() {
  if (!canSend.value) {
    return;
  }

  const content = input.value.trim();
  messages.value.push({ role: 'user', content, time: dayjs().format('HH:mm') });
  input.value = '';
  loading.value = true;

  try {
    const response = await sendChatMessage({ message: content, sessionId: sessionId.value });
    sessionId.value = response.sessionId;
    messages.value.push({
      role: 'assistant',
      content: response.answer,
      time: dayjs(response.createdAt).format('HH:mm'),
    });
  } catch (error) {
    antMessage.error('请求后端失败，请确认 Spring Boot 服务已启动');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="page-shell">
    <section class="hero-card">
      <div class="hero-copy">
        <div class="eyebrow"><ThunderboltOutlined /> AI 运维工作台</div>
        <h1>Spring AI Ops Copilot</h1>
        <p>把告警、日志、知识库和变更记录收束到一个可执行的排障对话入口。</p>
      </div>
      <div class="status-grid">
        <a-statistic title="Provider" value="OpenAI" />
        <a-statistic title="Vector DB" value="pgvector" />
        <a-statistic title="Cache" value="Redis" />
      </div>
    </section>

    <section class="workspace">
      <aside class="side-panel">
        <h2>建议场景</h2>
        <button @click="input = '服务 5xx 突增，帮我设计排查路径'">5xx 突增排查</button>
        <button @click="input = 'Redis 延迟升高，应该先看哪些指标？'">Redis 延迟分析</button>
        <button @click="input = '生成一次数据库扩容变更的风险清单'">变更风险清单</button>
      </aside>

      <section class="chat-card">
        <header>
          <div>
            <span class="bot-mark"><RobotOutlined /></span>
            <strong>Copilot Session</strong>
          </div>
          <small>{{ sessionId || 'new session' }}</small>
        </header>

        <div class="message-list">
          <article v-for="(item, index) in messages" :key="index" :class="['message', item.role]">
            <span class="time">{{ item.time }}</span>
            <p>{{ item.content }}</p>
          </article>
          <a-skeleton v-if="loading" active :paragraph="{ rows: 2 }" />
        </div>

        <footer class="composer">
          <a-textarea
            v-model:value="input"
            :auto-size="{ minRows: 2, maxRows: 5 }"
            placeholder="输入告警、日志片段或运维问题..."
            @press-enter.prevent="handleSend"
          />
          <a-button type="primary" size="large" :loading="loading" :disabled="!canSend" @click="handleSend">
            <template #icon><SendOutlined /></template>
            发送
          </a-button>
        </footer>
      </section>
    </section>
  </main>
</template>
