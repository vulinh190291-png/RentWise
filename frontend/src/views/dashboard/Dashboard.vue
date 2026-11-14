<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { getMyProfile, ProfileNotInitializedError } from '../../api/profile'
import { getNextTraining } from '../../api/plan'

import type {
  Difficulty,
  NextTrainingResponse,
  RiskTopic,
  UserProfileResponse,
} from '../../types/api'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const dataLoading = ref(true)

const profile = ref<UserProfileResponse | null>(null)
const recommendation = ref<NextTrainingResponse | null>(null)

const profileError = ref('')
const profileNotInitialized = ref(false)
const recommendationError = ref('')

const topicNames: Record<RiskTopic, string> = {
  DEPOSIT_RETURN: '押金返还',
  EARLY_TERMINATION: '提前退租',
  REPAIR_RESPONSIBILITY: '维修责任',
  COST_BEARING: '费用承担',
  BREACH_LIABILITY: '违约责任',
}

const difficultyNames: Record<Difficulty, string> = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难',
}

function getErrorMessage(error: unknown) {
  return error instanceof Error
      ? error.message
      : '加载失败，请稍后重试'
}

// 平均掌握度
const averageMastery = computed(() => {
  const topics = profile.value?.topics ?? []

  if (topics.length === 0) return 0

  const total = topics.reduce(
      (sum, item) => sum + item.masteryScore,
      0,
  )

  return Math.round(total / topics.length)
})

// 找出掌握度最低的主题
const weakestTopic = computed(() => {
  const topics = profile.value?.topics ?? []

  return [...topics].sort(
      (a, b) => a.masteryScore - b.masteryScore,
  )[0] ?? null
})

// 获取学习画像和推荐
async function loadLearningData() {
  dataLoading.value = true
  profileError.value = ''
  profileNotInitialized.value = false
  recommendationError.value = ''

  profile.value = null
  recommendation.value = null

  try {
    profile.value = await getMyProfile()
  } catch (error) {
    profileNotInitialized.value = error instanceof ProfileNotInitializedError
    profileError.value = getErrorMessage(error)
    dataLoading.value = false
    return
  }

  try {
    recommendation.value = await getNextTraining()
  } catch (error) {
    recommendationError.value = getErrorMessage(error)
  } finally {
    dataLoading.value = false
  }
}

// 页面初始化
onMounted(async () => {
  if (!authStore.user) {
    loading.value = true

    try {
      await authStore.loadCurrentUser()
    } catch {
      ElMessage.error('登录状态已失效，请重新登录')
      await router.replace('/login')
      return
    } finally {
      loading.value = false
    }
  }

  await loadLearningData()
})

function goDiagnosis() {
  router.push('/diagnosis')
}

function goResults() {
  router.push('/results')
}

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

      <router-link to="/explore">城市探索</router-link>
      <el-button type="danger" plain @click="handleLogout">
        退出登录
      </el-button>
    </header>

    <main class="content">

      <section class="welcome">
        <h1>
          欢迎回来，{{ authStore.user?.username || '学习者' }}
        </h1>
        <p>通过真实租赁场景训练，提高风险识别能力。</p>
      </section>

      <!-- 学习诊断入口 -->
      <el-card shadow="never" class="section-card">
        <h2>学习诊断</h2>
        <p>通过10道租赁条款识别题，了解自己的知识掌握情况。</p>

        <div class="actions">
          <el-button type="primary" @click="goDiagnosis">
            开始学习诊断
          </el-button>

          <el-button @click="goResults">
            查看完整学习画像
          </el-button>
        </div>
      </el-card>

      <!-- 加载中 -->
      <el-card v-if="dataLoading" shadow="never" class="section-card">
        <el-skeleton :rows="4" animated />
      </el-card>

      <!-- 未获得学习画像 -->
      <el-card
          v-else-if="!profile"
          shadow="never"
          class="section-card"
      >
        <el-empty :description="profileNotInitialized ? '尚无学习画像' : '学习画像加载失败'">
          <p v-if="profileError" class="hint">
            {{ profileError }}
          </p>

          <el-button v-if="profileNotInitialized" type="primary" @click="goDiagnosis">
            前往学习诊断
          </el-button>

          <el-button @click="loadLearningData">
            重新加载
          </el-button>
        </el-empty>
      </el-card>

      <!-- 已有学习画像 -->
      <template v-else>

        <section class="stats-grid">
          <el-card shadow="never">
            <el-statistic
                title="平均掌握度"
                :value="averageMastery"
                suffix="%"
            />
          </el-card>

          <el-card shadow="never">
            <el-statistic
                title="已分析知识主题"
                :value="profile.topics.length"
                suffix="个"
            />
          </el-card>
        </section>

        <el-card shadow="never" class="section-card">
          <template #header>
            <h2>知识主题掌握度</h2>
          </template>

          <div
              v-for="item in profile.topics"
              :key="item.topic"
              class="topic-item"
          >
            <div class="topic-heading">
              <span>
                {{ item.topicName || topicNames[item.topic] }}
              </span>
              <strong>{{ item.masteryScore }}%</strong>
            </div>

            <el-progress
                :percentage="item.masteryScore"
                :stroke-width="10"
                :show-text="false"
            />
          </div>

          <p class="hint">
            掌握度由后端根据学习记录计算，不等于单次正确率。
          </p>

          <el-button @click="goResults">
            查看详细分析
          </el-button>
        </el-card>

        <el-card shadow="never" class="section-card">
          <template #header>
            <h2>下一步学习建议</h2>
          </template>

          <template v-if="recommendation">
            <div class="recommendation-tags">
              <el-tag type="primary">
                {{ topicNames[recommendation.topic] }}
              </el-tag>

              <el-tag type="warning">
                {{ difficultyNames[recommendation.difficulty] }}难度
              </el-tag>
            </div>

            <p>
              {{ recommendation.reason }}
            </p>
          </template>

          <template v-else>
            <p v-if="weakestTopic">
              当前最低掌握度主题：
              <strong>{{ weakestTopic.topicName }}</strong>
              （{{ weakestTopic.masteryScore }}%）
            </p>

            <p class="hint">
              {{ recommendationError || '暂无训练推荐' }}
            </p>
          </template>

          <el-button type="primary" @click="goResults">
            查看推荐训练详情
          </el-button>
        </el-card>
      </template>

      <!-- 其他功能 -->
      <section class="modules">
        <h2>其他训练模块</h2>

        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-card shadow="hover">
              <h3>自适应训练</h3>
              <p>围绕薄弱主题进行针对性练习。</p>
              <el-button
                  type="primary"
                  @click="router.push('/training')"
              >
                开始自适应训练
              </el-button>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-card shadow="hover">
              <h3>阶段测评</h3>
              <p>通过五个知识主题的测试，检验阶段学习成果。</p>

              <el-button
                  type="primary"
                  @click="router.push('/assessment')"
              >
                开始阶段测评
              </el-button>
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
  height: 68px;
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
  color: #1e293b;
}

.welcome p,
.section-card p,
.modules p,
.hint {
  color: #64748b;
  line-height: 1.8;
}

.section-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.section-card h2 {
  margin: 0 0 14px;
  color: #1e293b;
}

.actions,
.recommendation-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.topic-item {
  margin-bottom: 22px;
}

.topic-heading {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #334155;
}

.hint {
  font-size: 13px;
}

.modules {
  margin-top: 32px;
}

.modules h2 {
  margin-bottom: 18px;
}

@media (max-width: 600px) {
  .content {
    padding: 24px 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
