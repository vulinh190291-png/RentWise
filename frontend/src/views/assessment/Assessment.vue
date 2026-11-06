
<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import {
  startAssessment,
  finishAssessment,
} from '../../api/assessment'

import type {
  AssessmentResultResponse,
  CaseView,
  RiskTopic,
} from '../../types/api'

const router = useRouter()

type PageState = 'intro' | 'answering' | 'completed'

const pageState = ref<PageState>('intro')
const starting = ref(false)
const submitting = ref(false)
const errorMessage = ref('')

const sessionId = ref<number | null>(null)
const cases = ref<CaseView[]>([])
const currentIndex = ref(0)

// 每道题的选择，null 表示未作答
const answers = ref<(boolean | null)[]>([])

const completion = ref<AssessmentResultResponse | null>(null)

const topicNames: Record<RiskTopic, string> = {
  DEPOSIT_RETURN: '押金返还',
  EARLY_TERMINATION: '提前退租',
  REPAIR_RESPONSIBILITY: '维修责任',
  COST_BEARING: '费用承担',
  BREACH_LIABILITY: '违约责任',
}

const currentCase = computed(() => {
  return cases.value[currentIndex.value] ?? null
})

const currentAnswer = computed(() => {
  return answers.value[currentIndex.value] ?? null
})

const answeredCount = computed(() => {
  return answers.value.filter(answer => answer !== null).length
})

const progressPercent = computed(() => {
  if (cases.value.length === 0) return 0

  return Math.round(
      answeredCount.value / cases.value.length * 100,
  )
})

const averageMastery = computed(() => {
  const topics = completion.value?.profile.topics ?? []

  if (topics.length === 0) return 0

  const total = topics.reduce(
      (sum, topic) => sum + topic.masteryScore,
      0,
  )

  return Math.round(total / topics.length)
})

function getErrorMessage(error: unknown): string {
  return error instanceof Error
      ? error.message
      : '请求失败，请稍后重试'
}

async function handleStart() {
  if (starting.value) return

  starting.value = true
  errorMessage.value = ''

  try {
    const response = await startAssessment()

    const topicCount = new Set(
        response.cases.map(item => item.topic),
    ).size

    if (response.cases.length !== 5 || topicCount !== 5) {
      throw new Error('阶段测评题目异常，应覆盖五个不同主题')
    }

    sessionId.value = response.sessionId
    cases.value = response.cases
    answers.value = Array(response.cases.length).fill(null)

    currentIndex.value = 0
    completion.value = null
    pageState.value = 'answering'

    ElMessage.success('阶段测评已开始')
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
    ElMessage.error(errorMessage.value)
  } finally {
    starting.value = false
  }
}

function chooseAnswer(value: boolean) {
  if (submitting.value || pageState.value !== 'answering') return

  answers.value[currentIndex.value] = value
}

function handlePrevious() {
  if (submitting.value || currentIndex.value === 0) return

  currentIndex.value--
  errorMessage.value = ''
}

async function handleNext() {
  if (submitting.value || currentAnswer.value === null) return

  if (currentIndex.value < cases.value.length - 1) {
    currentIndex.value++
    errorMessage.value = ''
    return
  }

  await handleFinish()
}

async function handleFinish() {
  if (submitting.value || sessionId.value === null) return

  if (
      cases.value.length !== 5 ||
      answers.value.length !== cases.value.length ||
      answers.value.some(answer => answer === null)
  ) {
    ElMessage.warning('请先完成全部五道测评题')
    return
  }

  submitting.value = true
  errorMessage.value = ''

  try {
    const request = {
      answers: cases.value.map((item, index) => ({
        caseId: item.caseId,
        selectedClarify: answers.value[index] as boolean,
      })),
    }

    completion.value = await finishAssessment(
        sessionId.value,
        request,
    )

    pageState.value = 'completed'
    ElMessage.success('阶段测评完成，学习画像已更新')
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
    ElMessage.error(errorMessage.value)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="assessment-page">
    <header class="topbar">
      <strong class="brand">RentWise</strong>

      <el-button @click="router.push('/dashboard')">
        返回学习中心
      </el-button>
    </header>

    <main class="content">
      <div class="page-heading">
        <el-tag type="primary">ASSESSMENT</el-tag>
        <h1>阶段学习测评</h1>
        <p>通过五道主题测评题，检验当前的租赁风险识别能力。</p>
      </div>

      <!-- 测评介绍 -->
      <el-card
          v-if="pageState === 'intro'"
          class="panel"
          shadow="never"
      >
        <h2>测评说明</h2>

        <p>
          本次测评共 5 道题，分别覆盖押金返还、
          提前退租、维修责任、费用承担和违约责任。
        </p>

        <p>
          每道题需要判断合同条款是否值得进一步澄清。
          五道题完成后统一提交。
        </p>

        <el-alert
            title="测评结果将参与学习画像计算"
            description="提交成功后，后端会更新你的主题掌握度与后续训练推荐。"
            type="info"
            :closable="false"
            show-icon
        />

        <el-alert
            v-if="errorMessage"
            class="message"
            title="暂时无法开始测评"
            :description="errorMessage"
            type="warning"
            :closable="false"
        />

        <div class="actions">
          <el-button
              type="primary"
              size="large"
              :loading="starting"
              @click="handleStart"
          >
            开始五题测评
          </el-button>

          <el-button @click="router.push('/diagnosis')">
            前往初始诊断
          </el-button>
        </div>
      </el-card>

      <!-- 正式答题 -->
      <template v-else-if="pageState === 'answering' && currentCase">
        <el-card class="panel" shadow="never">
          <div class="progress-heading">
            <strong>
              第 {{ currentIndex + 1 }} / {{ cases.length }} 题
            </strong>

            <span>已作答 {{ answeredCount }} 题</span>
          </div>

          <el-progress
              :percentage="progressPercent"
              :stroke-width="10"
          />

          <div class="topic">
            <el-tag>
              {{ topicNames[currentCase.topic] }}
            </el-tag>

            <el-tag type="info">
              阶段测评
            </el-tag>
          </div>

          <h2>租赁合同条款</h2>

          <div class="clause-box">
            {{ currentCase.clauseText }}
          </div>

          <h3 class="question">
            {{ currentCase.question }}
          </h3>

          <div class="answer-options">
            <el-button
                :type="currentAnswer === true ? 'primary' : 'default'"
                :disabled="submitting"
                @click="chooseAnswer(true)"
            >
              需要进一步澄清
            </el-button>

            <el-button
                :type="currentAnswer === false ? 'primary' : 'default'"
                :disabled="submitting"
                @click="chooseAnswer(false)"
            >
              不需要进一步澄清
            </el-button>
          </div>

          <el-alert
              v-if="errorMessage"
              class="message"
              :title="errorMessage"
              type="error"
              :closable="false"
          />

          <div class="actions">
            <el-button
                :disabled="currentIndex === 0 || submitting"
                @click="handlePrevious"
            >
              上一题
            </el-button>

            <el-button
                type="primary"
                :loading="submitting"
                :disabled="currentAnswer === null"
                @click="handleNext"
            >
              {{
                currentIndex === cases.length - 1
                    ? '提交全部答案'
                    : '下一题'
              }}
            </el-button>
          </div>
        </el-card>
      </template>

      <!-- 测评完成 -->
      <el-card
          v-else-if="pageState === 'completed' && completion"
          class="panel"
          shadow="never"
      >
        <el-result
            icon="success"
            title="阶段测评完成"
            sub-title="五个主题的答题记录已提交，学习画像已更新。"
        />

        <div class="summary">
          <el-statistic
              title="完成题数"
              :value="cases.length"
              suffix="题"
          />

          <el-statistic
              title="当前平均掌握度"
              :value="averageMastery"
              suffix="%"
          />
        </div>

        <h3>更新后的主题掌握度</h3>

        <div
            v-for="item in completion.profile.topics"
            :key="item.topic"
            class="mastery-row"
        >
          <div class="mastery-label">
            <span>{{ item.topicName }}</span>
            <strong>{{ item.masteryScore }}%</strong>
          </div>

          <el-progress
              :percentage="item.masteryScore"
              :stroke-width="10"
              :show-text="false"
          />
        </div>

        <el-divider />

        <h3>下一步训练建议</h3>

        <p>
          推荐主题：
          <strong>
            {{ topicNames[completion.nextTraining.topic] }}
          </strong>
        </p>

        <p>
          {{ completion.nextTraining.reason }}
        </p>

        <div class="actions">
          <el-button @click="router.push('/results')">
            查看学习画像
          </el-button>

          <el-button
              type="primary"
              @click="router.push('/training')"
          >
            继续自适应训练
          </el-button>
        </div>

        <p class="note">
          当前展示的是综合学习掌握度，不等于本次五题的正确率。
        </p>
      </el-card>
    </main>
  </div>
</template>

<style scoped>
.assessment-page {
  min-height: 100vh;
  background: #f4f7fb;
}

.topbar {
  height: 68px;
  padding: 0 6%;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e5eaf0;
}

.brand {
  font-size: 22px;
  color: #2563eb;
}

.content {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 24px 64px;
}

.page-heading {
  margin-bottom: 28px;
}

.page-heading h1 {
  margin: 14px 0;
  color: #1e293b;
}

.page-heading p,
.panel p,
.note {
  color: #64748b;
  line-height: 1.8;
}

.panel {
  border-radius: 12px;
}

.panel h2,
.panel h3 {
  color: #334155;
}

.progress-heading,
.mastery-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.progress-heading {
  color: #64748b;
}

.topic {
  display: flex;
  gap: 10px;
  margin: 26px 0;
}

.clause-box {
  background: #f8fafc;
  border-left: 4px solid #2563eb;
  border-radius: 8px;
  padding: 24px;
  line-height: 1.9;
  white-space: pre-wrap;
}

.question {
  margin: 28px 0 20px;
  line-height: 1.7;
}

.answer-options {
  display: grid;
  gap: 14px;
}

.answer-options .el-button {
  width: 100%;
  min-height: 54px;
  margin-left: 0;
  white-space: normal;
}

.actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 28px;
}

.message {
  margin-top: 20px;
}

.summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin: 16px 0 32px;
  padding: 24px;
  background: #f8fafc;
  border-radius: 12px;
}

.mastery-row {
  margin-bottom: 20px;
}

.note {
  margin-top: 24px;
  font-size: 13px;
}

@media (max-width: 600px) {
  .content {
    padding: 24px 16px;
  }

  .summary {
    grid-template-columns: 1fr;
  }
}
</style>
