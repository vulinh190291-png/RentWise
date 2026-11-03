
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { register as registerApi } from '../../api/auth'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '用户名长度为 3～32 位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 64, message: '密码长度为 8～64 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function handleRegister() {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true

  try {
    await registerApi({
      username: form.username.trim(),
      password: form.password,
    })

    ElMessage.success('注册成功，请登录！')
    await router.push('/login')
  } catch (error) {
    const message = error instanceof Error
        ? error.message
        : '注册失败，请稍后重试'

    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <div class="brand">
        <div class="brand-icon">R</div>
        <h1>RentWise</h1>
        <p>Personalized Learning Platform</p>
      </div>

      <div class="form-title">
        <h2>创建账户</h2>
        <p>开启你的个性化学习之旅。</p>
      </div>

      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          @keyup.enter="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名（3～32 位）"
              size="large"
              :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（8～64 位）"
              size="large"
              show-password
              :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
              :prefix-icon="Lock"
          />
        </el-form-item>

        <el-button
            type="primary"
            size="large"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
        >
          创 建 账 户
        </el-button>
      </el-form>

      <div class="login-link">
        已经有账户？
        <RouterLink to="/login">返回登录</RouterLink>
      </div>
    </div>

    <div class="footer">
      RentWise · Learn Smarter, Grow Better
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 24px;
  background: #f4f7fb;
}

.register-card {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(30, 50, 80, 0.08);
}

.brand {
  text-align: center;
  margin-bottom: 32px;
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

.register-button {
  width: 100%;
  margin-top: 8px;
}

.login-link {
  margin-top: 24px;
  text-align: center;
  color: #64748b;
  font-size: 14px;
}

.login-link a {
  color: #2563eb;
  text-decoration: none;
}

.footer {
  margin-top: 28px;
  color: #94a3b8;
  font-size: 12px;
}
</style>
