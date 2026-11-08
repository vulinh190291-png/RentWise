
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { getNextTraining } from '../../api/plan'
import { submitTrainingAnswer } from '../../api/training'

import type {
  Difficulty,
  NextTrainingResponse,
  RiskTopic,
  TrainingAnswerResponse,
} from '../../types/api'

const router = useRouter()
const submissionUncertain = ref(false)
const loading = ref(true)
const submitting = ref(false)
const errorMessage = ref('')

const recommendation = ref<NextTrainingResponse | null>(null)
const selectedClarify = ref<boolean | null>(null)
const result = ref<TrainingAnswerResponse | null>(null)

// 当前页面会话中完成的训练题数
const completedCount = ref(0)

const PENDING_SUBMISSION_KEY =
    'rentwise_training_pending_submission'

interface PendingSubmission {
  requestId: string
  caseId: number
  selectedClarify: boolean
}

function readPendingSubmission(): PendingSubmission | null {
  const raw = sessionStorage.getItem(PENDING_SUBMISSION_KEY)

  if (!raw) return null

  try {
    const value = JSON.parse(raw) as PendingSubmission

    if (
        typeof value.requestId === 'string' &&
        typeof value.caseId === 'number' &&
        typeof value.selectedClarify === 'boolean'
    ) {
      return value
    }
  } catch {
    // 忽略格式不正确的本地数据
  }

  sessionStorage.removeItem(PENDING_SUBMISSION_KEY)
  return null
}

function getOrCreateSubmission(
    caseId: number,
    selectedClarify: boolean,
): PendingSubmission {
  const existing = readPendingSubmission()

  if (existing && existing.caseId === caseId) {
    if (existing.selectedClarify !== selectedClarify) {
      throw new Error(
          '本题已有未确认的提交，请使用原来的答案重试',
      )
    }

    return existing
  }

  const submission: PendingSubmission = {
    requestId: crypto.randomUUID(),
    caseId,
    selectedClarify,
  }

  sessionStorage.setItem(
      PENDING_SUBMISSION_KEY,
      JSON.stringify(submission),
  )

  return submission
}


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

const currentCase = computed(() => {
  return recommendation.value?.trainingCase ?? null
})

function getErrorMessage(error: unknown): string {
  return error instanceof Error
      ? error.message
      : '操作失败，请稍后重试'
}

// 加载后端推荐的第一道题
async function loadTraining() {
  if (loading.value && recommendation.value) return

  loading.value = true
  errorMessage.value = ''

  try {
    const next = await getNextTraining()

    recommendation.value = next
    result.value = null

    const pending = readPendingSubmission()

    if (
        pending &&
        pending.caseId === next.trainingCase.caseId
    ) {
      selectedClarify.value = pending.selectedClarify
      submissionUncertain.value = true

      errorMessage.value =
          '检测到一笔尚未确认的提交，可以安全重试。'
    } else {
      selectedClarify.value = null
      submissionUncertain.value = false
    }

    result.value = null
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    loading.value = false
  }
}

// 提交用户选择


async function handleSubmit() {
  if (
      submitting.value ||
      result.value ||
      selectedClarify.value === null ||
      !currentCase.value
  ) {
    return
  }

  submitting.value = true
  errorMessage.value = ''

  try {
    const submission = getOrCreateSubmission(
        currentCase.value.caseId,
        selectedClarify.value,
    )

    const response = await submitTrainingAnswer(
        submission,
    )

    result.value = response
    completedCount.value++
    submissionUncertain.value = false

    // 已经得到明确的后端响应，
    // 不再保留待确认请求
    sessionStorage.removeItem(PENDING_SUBMISSION_KEY)

    if (response.syncStatus === 'PENDING') {
      ElMessage.warning(
          '答案已保存，学习画像正在后台同步',
      )
    } else {
      ElMessage.success(
          '答案已提交，学习画像已更新',
      )
    }

  } catch (error) {
    // 不能确定服务端是否已提交成功；
    // 但下一次请求会使用同一个 requestId。
    submissionUncertain.value = true

    errorMessage.value =
        '提交结果尚未确认，可使用原请求编号安全重试。'

    ElMessage.warning(errorMessage.value)
    console.error(error)

  } finally {
    submitting.value = false
  }
}


// 使用提交接口返回的下一题，不再重复请求

async function handleNext() {
  if (!result.value) return

  if (result.value.nextTraining) {
    recommendation.value = result.value.nextTraining
    result.value = null
    selectedClarify.value = null
    errorMessage.value = ''
    submissionUncertain.value = false
    return
  }

  // 画像同步中：尝试重新获取推荐题
  // 如果 Profile 尚未恢复，页面会展示加载错误
  await loadTraining()
}


onMounted(loadTraining)
</script>

<template>
  <div class="training-page">

    <header class="header">
      <div class="brand">
        <span class="logo">R</span>
        <span>RentWise</span>
      </div>

      <el-button @click="router.push('/dashboard')">
        返回 Dashboard
      </el-button>
    </header>

    <main class="content">
      <div class="page-heading">
        <el-tag type="success">ADAPTIVE TRAINING</el-tag>

        <h1>自适应租赁风险训练</h1>

        <p>
          系统根据你的知识掌握情况，
          推荐更适合当前阶段的练习。
        </p>
      </div>

      <!-- 初次加载 -->
      <el-card v-if="loading" shadow="never">
        <el-skeleton :rows="7" animated />
      </el-card>

      <!-- 未获得推荐题 -->
      <el-card
          v-else-if="!recommendation"
          shadow="never"
      >
        <el-alert
            title="暂时无法加载训练题"
            :description="errorMessage"
            type="warning"
            :closable="false"
            show-icon
        />

        <div class="actions">
          <el-button type="primary" @click="loadTraining">
            重新加载
          </el-button>

          <el-button @click="router.push('/diagnosis')">
            前往初始诊断
          </el-button>
        </div>
      </el-card>

      <template v-else-if="currentCase">

        <!-- 学习概览 -->
        <div class="training-summary">
          <span>
            本次已完成 {{ completedCount }} 道训练题
          </span>

          <el-button
              text
              type="primary"
              @click="router.push('/results')"
          >
            查看学习画像
          </el-button>
        </div>

        <el-card shadow="never" class="training-card">

          <div class="tags">
            <el-tag type="primary">
              {{ topicNames[recommendation.topic] }}
            </el-tag>

            <el-tag type="warning">
              {{ difficultyNames[recommendation.difficulty] }}难度
            </el-tag>
          </div>

          <p class="reason">
            推荐原因：{{ recommendation.reason }}
          </p>

          <!-- 可选的学习卡片 -->
          <div
              v-if="recommendation.learningCard"
              class="learning-card"
          >
            <h3>{{ recommendation.learningCard.title }}</h3>
            <p>{{ recommendation.learningCard.content }}</p>
          </div>

          <el-divider />

          <h2>租赁合同条款</h2>

          <div class="clause-box">
            {{ currentCase.clauseText }}
          </div>

          <h3 class="question">
            {{ currentCase.question }}
          </h3>

          <div class="answer-options">
            <el-button
                class="answer-option"
                :type="selectedClarify === true ? 'primary' : 'default'"
                :disabled="submitting || !!result || submissionUncertain"
                @click="selectedClarify = true"
            >
              需要进一步澄清
            </el-button>

            <el-button
                class="answer-option"
                :type="selectedClarify === false ? 'primary' : 'default'"
                :disabled="submitting || !!result || submissionUncertain"
                @click="selectedClarify = false"
            >
              不需要进一步澄清
            </el-button>
          </div>

          <!-- 答题后显示反馈 -->

          <!-- 答题后显示反馈 -->
          <div v-if="result" class="feedback-area">

            <el-alert
                :title="result.answer.correct ? '回答正确' : '回答不正确'"
                :type="result.answer.correct ? 'success' : 'error'"
                :closable="false"
                show-icon
            />

            <h3>条款分析</h3>

            <p class="explanation">
              {{ result.feedback.explanation }}
            </p>

            <div
                v-if="result.feedback.suggestedQuestion"
                class="follow-up"
            >
              <strong>建议向出租方追问：</strong>
              <p>{{ result.feedback.suggestedQuestion }}</p>
            </div>

            <el-divider />

            <!-- 画像已经同步成功 -->
            <template v-if="result.updatedMastery">
              <h3>更新后的掌握度</h3>

              <p>
                {{ result.updatedMastery.topicName }}
                · {{ result.updatedMastery.masteryScore }}%
              </p>

              <el-progress
                  :percentage="result.updatedMastery.masteryScore"
                  :stroke-width="12"
              />
            </template>

            <!-- 画像还在后台同步 -->
            <el-alert
                v-else
                title="答案已保存，学习画像正在同步"
                description="Profile 服务暂时不可用，后台将尝试恢复同步。"
                type="warning"
                :closable="false"
                show-icon
            />

            <p v-if="result.nextTraining" class="next-tip">
              下一题推荐：
              {{ topicNames[result.nextTraining.topic] }}
              ·
              {{ difficultyNames[result.nextTraining.difficulty] }}
            </p>

          </div>


          <el-alert
              v-if="errorMessage"
              :title="errorMessage"
              type="error"
              :closable="false"
              show-icon
              class="error-alert"
          />

          <div class="actions">
            <el-button
                v-if="!result"
                type="primary"
                size="large"
                :loading="submitting"
                :disabled="selectedClarify === null"
                @click="handleSubmit"
            >
              {{ submissionUncertain ? '安全重试本次提交' : '提交答案' }}
            </el-button>

            <template v-else>
              <el-button @click="router.push('/results')">
                查看学习画像
              </el-button>

              <el-button
                  type="primary"
                  size="large"
                  @click="handleNext"
              >
                {{ result.nextTraining ? '继续下一题' : '重新获取推荐' }}
              </el-button>
            </template>
          </div>

        </el-card>
      </template>

    </main>
  </div>
</template>

<style scoped>
.training-page {
  min-height: 100vh;
  background: #f4f7fb;
}

.header {
  height: 68px;
  padding: 0 6%;
  background: white;
  border-bottom: 1px solid #e5eaf0;
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  background: #2563eb;
  color: white;
  border-radius: 10px;
  display: grid;
  place-items: center;
}

.content {
  max-width: 920px;
  margin: 0 auto;
  padding: 40px 24px 64px;
}

.page-heading {
  margin-bottom: 26px;
}

.page-heading h1 {
  color: #1e293b;
}

.page-heading p,
.reason,
.training-summary,
.next-tip {
  color: #64748b;
  line-height: 1.8;
}

.training-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.training-card {
  border-radius: 12px;
}

.tags {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.reason {
  margin-top: 18px;
}

.clause-box,
.learning-card,
.follow-up {
  background: #f8fafc;
  border-radius: 10px;
  padding: 20px;
  line-height: 1.9;
  white-space: pre-wrap;
}

.clause-box {
  border-left: 4px solid #2563eb;
}

.learning-card,
.follow-up {
  margin-top: 20px;
  border: 1px solid #e5eaf0;
}

.question {
  margin: 28px 0 20px;
  color: #334155;
  line-height: 1.7;
}

.answer-options {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.answer-option {
  width: 100%;
  min-height: 52px;
  margin-left: 0 !important;
  justify-content: flex-start;
  white-space: normal;
}

.feedback-area {
  margin-top: 28px;
}

.feedback-area h3 {
  margin-top: 24px;
  color: #334155;
}

.explanation {
  line-height: 1.9;
  white-space: pre-wrap;
}

.next-tip {
  margin-top: 18px;
}

.error-alert {
  margin-top: 20px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
  flex-wrap: wrap;
}

@media (max-width: 600px) {
  .content {
    padding: 24px 16px;
  }

  .training-summary {
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
