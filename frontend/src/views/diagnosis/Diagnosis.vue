
<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import {
  startDiagnosis,
  submitDiagnosisAnswer,
  finishDiagnosis,
} from '../../api/diagnosis'

import type {
  AnswerResult,
  CaseView,
  DiagnosisCompleteResponse,
  RiskTopic,
} from '../../types/api'

const router = useRouter()

type PageState = 'intro' | 'answering' | 'completed'

const pageState = ref<PageState>('intro')

const starting = ref(false)
const submitting = ref(false)
const finishing = ref(false)

const errorMessage = ref('')

const sessionId = ref<number | null>(null)
const cases = ref<CaseView[]>([])
const currentIndex = ref(0)

const selectedClarify = ref<boolean | null>(null)
const answerResult = ref<AnswerResult | null>(null)

const completion = ref<DiagnosisCompleteResponse | null>(null)

// 当前题目
const currentCase = computed(() => {
  return cases.value[currentIndex.value] ?? null
})

// 已经提交的题目数量
const answeredCount = computed(() => {
  if (pageState.value === 'completed') {
    return cases.value.length
  }

  return currentIndex.value + (answerResult.value ? 1 : 0)
})

// 答题进度
const progressPercent = computed(() => {
  if (cases.value.length === 0) return 0

  return Math.round(
      (answeredCount.value / cases.value.length) * 100,
  )
})

// 知识主题中文名称
const topicNames: Record<RiskTopic, string> = {
  DEPOSIT_RETURN: '押金返还',
  EARLY_TERMINATION: '提前退租',
  REPAIR_RESPONSIBILITY: '维修责任',
  COST_BEARING: '费用承担',
  BREACH_LIABILITY: '违约责任',
}

// 难度中文名称
const difficultyNames = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难',
}

// 获取主题名称
function getTopicName(topic: RiskTopic) {
  return topicNames[topic] ?? topic
}

// 获取难度名称
function getDifficultyName(difficulty: CaseView['difficulty']) {
  return difficultyNames[difficulty]
}

// 获取错误信息
function getErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message
  }

  return '请求失败，请稍后重试'
}

// 开始诊断
async function handleStart() {
  starting.value = true
  errorMessage.value = ''

  try {
    const response = await startDiagnosis()

    if (response.cases.length !== 10) {
      throw new Error(
          `诊断题目数量异常，预期10道，实际${response.cases.length}道`,
      )
    }

    sessionId.value = response.sessionId
    cases.value = response.cases

    currentIndex.value = 0
    selectedClarify.value = null
    answerResult.value = null
    completion.value = null

    pageState.value = 'answering'

    ElMessage.success('诊断题目加载成功')
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
    ElMessage.error(errorMessage.value)
  } finally {
    starting.value = false
  }
}

// 提交答案
async function handleSubmit() {
  if (
      sessionId.value === null ||
      currentCase.value === null ||
      selectedClarify.value === null
  ) {
    return
  }

  submitting.value = true
  errorMessage.value = ''

  try {
    const result = await submitDiagnosisAnswer(
        sessionId.value,
        {
          caseId: currentCase.value.caseId,
          selectedClarify: selectedClarify.value,
        },
    )

    answerResult.value = result

  } catch (error) {
    errorMessage.value = getErrorMessage(error)
    ElMessage.error(errorMessage.value)
  } finally {
    submitting.value = false
  }
}

// 进入下一题，或者完成诊断
async function handleContinue() {
  if (!answerResult.value) return

  if (currentIndex.value < cases.value.length - 1) {
    currentIndex.value++

    selectedClarify.value = null
    answerResult.value = null
    errorMessage.value = ''

    return
  }

  await handleFinish()
}

// 完成诊断
async function handleFinish() {
  if (sessionId.value === null) return

  finishing.value = true
  errorMessage.value = ''

  try {
    completion.value = await finishDiagnosis(sessionId.value)

    pageState.value = 'completed'

    ElMessage.success('诊断完成，学习画像已生成')
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
    ElMessage.error(errorMessage.value)
  } finally {
    finishing.value = false
  }
}

// 返回 Dashboard
function goDashboard() {
  router.push('/dashboard')
}
</script>

<template>
  <div class="diagnosis-page">

    <header class="page-header">
      <div class="brand">
        <span class="logo">R</span>
        <span>RentWise</span>
      </div>

      <el-button plain @click="goDashboard">
        返回 Dashboard
      </el-button>
    </header>

    <main class="content">

      <!-- 诊断介绍 -->
      <section v-if="pageState === 'intro'">

        <div class="page-title">
          <el-tag type="success">LEARNING DIAGNOSIS</el-tag>

          <h1>租赁风险知识诊断</h1>

          <p>
            通过10道真实租赁场景题，
            了解你对租赁风险知识的掌握情况。
          </p>
        </div>

        <el-card shadow="never" class="intro-card">

          <h2>本次诊断内容</h2>

          <div class="topic-grid">
            <div class="topic-item">
              <span>01</span>
              <strong>押金返还</strong>
            </div>

            <div class="topic-item">
              <span>02</span>
              <strong>提前退租</strong>
            </div>

            <div class="topic-item">
              <span>03</span>
              <strong>维修责任</strong>
            </div>

            <div class="topic-item">
              <span>04</span>
              <strong>费用承担</strong>
            </div>

            <div class="topic-item">
              <span>05</span>
              <strong>违约责任</strong>
            </div>
          </div>

          <el-divider />

          <div class="diagnosis-info">
            <div>
              <strong>10 道</strong>
              <span>诊断题目</span>
            </div>

            <div>
              <strong>5 个</strong>
              <span>知识主题</span>
            </div>

            <div>
              <strong>1 次</strong>
              <span>完整诊断</span>
            </div>
          </div>

          <el-button
              type="primary"
              size="large"
              :loading="starting"
              @click="handleStart"
          >
            开始学习诊断
          </el-button>

        </el-card>

      </section>

      <!-- 正式答题 -->
      <section v-else-if="pageState === 'answering'">

        <div class="question-header">
          <div>
            <el-tag type="success">LEARNING DIAGNOSIS</el-tag>
            <h1>租赁风险知识诊断</h1>
          </div>

          <span class="question-count">
            {{ answeredCount }} / {{ cases.length }}
          </span>
        </div>

        <el-card shadow="never" class="question-card">

          <div class="progress-header">
            <span>答题进度</span>
            <strong>{{ progressPercent }}%</strong>
          </div>

          <el-progress
              :percentage="progressPercent"
              :stroke-width="10"
              :show-text="false"
          />

          <div v-if="currentCase" class="question-content">

            <div class="question-tags">
              <el-tag type="info">
                {{ getTopicName(currentCase.topic) }}
              </el-tag>

              <el-tag
                  :type="
                  currentCase.difficulty === 'EASY'
                    ? 'success'
                    : currentCase.difficulty === 'MEDIUM'
                      ? 'warning'
                      : 'danger'
                "
              >
                {{ getDifficultyName(currentCase.difficulty) }}
              </el-tag>
            </div>

            <div class="question-number">
              QUESTION {{ currentIndex + 1 }}
            </div>

            <h2>租赁合同条款</h2>

            <div class="clause-box">
              {{ currentCase.clauseText }}
            </div>

            <h3>{{ currentCase.question }}</h3>

            <div class="answer-options">

              <el-button
                  class="answer-option"
                  :type="selectedClarify === true ? 'primary' : 'default'"
                  :plain="selectedClarify !== true"
                  :disabled="!!answerResult"
                  @click="selectedClarify = true"
              >
                需要进一步澄清
              </el-button>

              <el-button
                  class="answer-option"
                  :type="selectedClarify === false ? 'primary' : 'default'"
                  :plain="selectedClarify !== false"
                  :disabled="!!answerResult"
                  @click="selectedClarify = false"
              >
                不需要进一步澄清
              </el-button>

            </div>

            <!-- 后端答题反馈 -->
            <el-alert
                v-if="answerResult"
                :title="
                answerResult.correct
                  ? '回答正确'
                  : '回答不正确'
              "
                :type="answerResult.correct ? 'success' : 'error'"
                :closable="false"
                show-icon
                class="answer-feedback"
            />

            <el-alert
                v-if="errorMessage"
                :title="errorMessage"
                type="error"
                :closable="false"
                show-icon
                class="answer-feedback"
            />

            <div class="action-area">

              <el-button
                  v-if="!answerResult"
                  type="primary"
                  size="large"
                  :disabled="selectedClarify === null"
                  :loading="submitting"
                  @click="handleSubmit"
              >
                提交答案
              </el-button>

              <el-button
                  v-else
                  type="primary"
                  size="large"
                  :loading="finishing"
                  @click="handleContinue"
              >
                {{
                  currentIndex === cases.length - 1
                      ? '完成诊断'
                      : '下一题'
                }}
              </el-button>

            </div>

          </div>

        </el-card>

      </section>

      <!-- 诊断完成 -->
      <section v-else-if="pageState === 'completed'">

        <el-result
            icon="success"
            title="学习诊断已完成"
            sub-title="10道题目已全部提交，后端已生成你的学习画像和训练计划。"
        >

          <template #extra>

            <div
                v-if="completion"
                class="completion-info"
            >
              <el-tag type="success">
                已分析 {{ completion.profile.topics.length }} 个知识主题
              </el-tag>

              <el-tag type="info">
                推荐关注：{{ getTopicName(completion.plan.focusTopic) }}
              </el-tag>
            </div>

            <el-button
                type="primary"
                @click="goDashboard"
            >
              返回 Dashboard
            </el-button>

          </template>

        </el-result>

      </section>

      <el-alert
          v-if="errorMessage && pageState !== 'answering'"
          :title="errorMessage"
          type="error"
          :closable="false"
          show-icon
      />

    </main>
  </div>
</template>

<style scoped>
.diagnosis-page {
  min-height: 100vh;
  background: #f4f7fb;
}

.page-header {
  height: 68px;
  padding: 0 6%;
  display: flex;
  align-items: center;
  justify-content: space-between;
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
  max-width: 900px;
  margin: 0 auto;
  padding: 48px 24px;
}

.page-title {
  margin-bottom: 28px;
}

.page-title h1,
.question-header h1 {
  font-size: 30px;
  color: #1e293b;
  margin: 16px 0 10px;
}

.page-title p {
  color: #64748b;
  line-height: 1.8;
}

.intro-card,
.question-card {
  border-radius: 12px;
}

.intro-card {
  padding: 20px;
}

.intro-card h2 {
  color: #1e293b;
  margin-bottom: 24px;
}

.topic-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.topic-item {
  padding: 18px;
  background: #f8fafc;
  border: 1px solid #e8edf4;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.topic-item span {
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
}

.topic-item strong {
  color: #334155;
}

.diagnosis-info {
  display: flex;
  justify-content: space-around;
  margin: 28px 0;
  text-align: center;
}

.diagnosis-info div {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.diagnosis-info strong {
  color: #2563eb;
  font-size: 24px;
}

.diagnosis-info span {
  color: #64748b;
  font-size: 13px;
}

.intro-card > .el-button {
  width: 100%;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.question-header h1 {
  font-size: 25px;
}

.question-count {
  color: #2563eb;
  font-size: 20px;
  font-weight: 700;
}

.question-card {
  padding: 24px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  color: #64748b;
}

.progress-header strong {
  color: #2563eb;
}

.question-content {
  margin-top: 32px;
}

.question-tags {
  display: flex;
  gap: 10px;
}

.question-number {
  margin-top: 32px;
  color: #94a3b8;
  font-size: 12px;
  letter-spacing: 2px;
  font-weight: 700;
}

.question-content h2 {
  margin: 12px 0 20px;
  color: #1e293b;
}

.clause-box {
  padding: 24px;
  background: #f8fafc;
  border-left: 4px solid #3985e8;
  border-radius: 6px;
  color: #334155;
  line-height: 2;
  font-size: 16px;
  white-space: pre-wrap;
}

.question-content h3 {
  margin: 28px 0 20px;
  color: #1e293b;
  line-height: 1.7;
}

.answer-options {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.answer-option {
  width: 100%;
  height: 52px;
  justify-content: flex-start;
  margin-left: 0 !important;
}

.answer-feedback {
  margin-top: 24px;
}

.action-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 28px;
}

.completion-info {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}

@media (max-width: 600px) {
  .content {
    padding: 28px 16px;
  }

  .topic-grid {
    grid-template-columns: 1fr;
  }

  .question-header {
    align-items: flex-start;
    gap: 12px;
  }

  .question-header h1 {
    font-size: 22px;
  }

  .question-card {
    padding: 16px;
  }

  .diagnosis-info strong {
    font-size: 19px;
  }
}
</style>