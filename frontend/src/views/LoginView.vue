<script setup lang="ts">
import { reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { LockOutlined, SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons-vue';
import { setAccessToken, setAuthUser } from '../auth/auth';
import { login } from '../api/auth';

const router = useRouter();
const loading = ref(false);

const formState = reactive({
  username: '',
  password: '',
});

async function handleLogin() {
  if (!formState.username.trim() || !formState.password.trim()) {
    message.warning('请输入用户名和密码');
    return;
  }

  loading.value = true;
  try {
    const result = await login({
      username: formState.username.trim(),
      password: formState.password,
    });

    setAccessToken(result.token);
    setAuthUser({
      userId: result.userId,
      username: result.username,
      nickname: result.nickname,
    });

    message.success('登录成功');
    router.push('/dashboard');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="auth-shell auth-shell--white">
    <section class="auth-stage">
      <div class="auth-decoration auth-decoration--one"></div>
      <div class="auth-decoration auth-decoration--two"></div>

      <section class="auth-copy-panel">
        <span class="auth-badge">
          <SafetyCertificateOutlined />
          Ops Copilot
        </span>
        <h1>运维协作入口</h1>
        <p>统一进入告警排查、知识库检索和会话协作工作台。当前先提供干净的登录骨架，便于后续继续接接口和权限。</p>

        <div class="auth-highlights">
          <div class="auth-highlight-card">
            <strong>知识沉淀</strong>
            <span>统一查看知识库与会话上下文</span>
          </div>
          <div class="auth-highlight-card">
            <strong>排障协作</strong>
            <span>快速进入聊天工作区处理运维问题</span>
          </div>
        </div>
      </section>

      <a-card class="auth-card auth-card--enhanced" :bordered="false">
        <div class="auth-card-head">
          <h2>登录系统</h2>
          <p>输入已有账号后进入控制台</p>
        </div>

        <a-form layout="vertical">
          <a-form-item label="用户名">
            <a-input v-model:value="formState.username" placeholder="请输入用户名">
              <template #prefix>
                <UserOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item label="密码">
            <a-input-password v-model:value="formState.password" placeholder="请输入密码">
              <template #prefix>
                <LockOutlined />
              </template>
            </a-input-password>
          </a-form-item>

          <a-button block type="primary" size="large" class="auth-submit-btn" :loading="loading" @click="handleLogin">
            登录
          </a-button>
        </a-form>
      </a-card>
    </section>
  </main>
</template>
