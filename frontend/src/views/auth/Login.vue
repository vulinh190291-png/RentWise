
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()

const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
}

async function handleLogin() {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await authStore.login({
      username: form.username,
      password: form.password,
    })

    ElMessage.success('登录成功，欢迎回来！')
    await router.push('/dashboard')
  } catch (error) {
    const message =
        error instanceof Error ? error.message : '登录失败，请检查账号密码'
    ElMessage.error(message)
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <div class="brand-icon">R</div>
        <h1>RentWise</h1>
        <p>Personalized Learning Platform</p>
      </div>

      <div class="form-title">
        <h2>欢迎回来</h2>
        <p>登录你的学习账户，继续成长。</p>
      </div>

      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              :prefix-icon="Lock"
          />
        </el-form-item>

        <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="authStore.loading"
            @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>

      <div class="register-link">
        还没有账户？
        <RouterLink to="/register">立即注册</RouterLink>
      </div>
    </div>

    <div class="footer">
      RentWise · Learn Smarter, Grow Better
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 24px;
  background: #f4f7fb;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(30, 50, 80, 0.08);
}

.brand {
  text-align: center;
  margin-bottom: 36px;
}

.brand-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 12px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: #2563eb;
  color: white;
  font-size: 25px;
  font-weight: 700;
}

.brand h1 {
  margin: 0;
  color: #1e293b;
  font-size: 25px;
}

.brand p {
  margin-top: 7px;
  color: #94a3b8;
  font-size: 12px;
}

.form-title {
  margin-bottom: 24px;
}

.form-title h2 {
  margin: 0 0 8px;
  color: #1e293b;
  font-size: 23px;
}

.form-title p {
  margin: 0;
  color: #94a3b8;
  font-size: 14px;
}

.login-button {
  width: 100%;
  margin-top: 8px;
}

.register-link {
  margin-top: 24px;
  text-align: center;
  color: #64748b;
  font-size: 14px;
}

.register-link a {
  color: #2563eb;
  text-decoration: none;
}

.footer {
  margin-top: 28px;
  color: #94a3b8;
  font-size: 12px;
}
</style>
