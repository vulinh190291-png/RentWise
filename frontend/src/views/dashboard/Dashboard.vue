// 进入学习诊断
function goDiagnosis() {
router.push('/diagnosis')
}
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)

// 进入学习诊断
function goDiagnosis() {
  router.push('/diagnosis')
}

// 页面初始化时获取用户信息
onMounted(async () => {
  if (!authStore.user) {
    loading.value = true

    try {
      await authStore.loadCurrentUser()
    } catch {
      ElMessage.error('登录状态已失效，请重新登录')
      await router.replace('/login')
    } finally {
      loading.value = false
    }
  }
})

// 退出登录
async function handleLogout() {
  authStore.logout()

  ElMessage.success('已退出登录')

  await router.replace('/login')
}
</script>

<template>
  <div class="dashboard">
    <header class="header">
      <div class="brand">
        <span class="logo">R</span>
        <span>RentWise</span>
      </div>

      <el-button type="danger" plain @click="handleLogout">
        退出登录
      </el-button>
    </header>

    <main class="content">
      <section class="welcome">
        <h1>欢迎回来，{{ authStore.user?.username }}</h1>
        <p>今天也继续你的学习成长之旅。</p>
      </section>

      <el-card shadow="hover">
        <h3>学习诊断</h3>
        <p>通过真实租赁场景，了解自己的知识掌握情况。</p>

        <el-button
            type="primary"
            @click="goDiagnosis"
        >
          进入诊断
        </el-button>
      </el-card>

      <section class="modules">
        <h2>学习中心</h2>

        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8">
            <el-card shadow="hover">
              <h3>学习诊断</h3>
              <p>了解自己的知识掌握情况。</p>
              <el-tag>待开发</el-tag>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="12" :md="8">
            <el-card shadow="hover">
              <h3>自适应训练</h3>
              <p>针对薄弱知识点开展训练。</p>
              <el-tag>待开发</el-tag>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="12" :md="8">
            <el-card shadow="hover">
              <h3>阶段测评</h3>
              <p>检验阶段性学习成果。</p>
              <el-tag>待开发</el-tag>
            </el-card>
          </el-col>
        </el-row>
      </section>
    </main>
  </div>
</template>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #f4f7fb;
}

.header {
  height: 64px;
  padding: 0 6%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border-bottom: 1px solid #e5eaf0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.logo {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  background: #2563eb;
  color: white;
  border-radius: 10px;
}

.content {
  max-width: 1100px;
  margin: 0 auto;
  padding: 40px 24px;
}

.welcome {
  margin-bottom: 28px;
}

.welcome h1 {
  font-size: 28px;
  color: #1e293b;
}

.welcome p,
.modules p {
  color: #64748b;
}

.modules {
  margin-top: 32px;
}

.modules h2 {
  margin-bottom: 20px;
}

.modules h3 {
  color: #1e293b;
}
</style>
