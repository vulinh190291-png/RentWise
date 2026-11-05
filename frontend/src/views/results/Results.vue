
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getMyProfile } from '../../api/profile'
import { getNextTraining } from '../../api/plan'

import type {
    Difficulty,
    NextTrainingResponse,
    RiskTopic,
    UserProfileResponse,
} from '../../types/api'

const router = useRouter()

const loading = ref(true)
const profile = ref<UserProfileResponse | null>(null)
const recommendation = ref<NextTrainingResponse | null>(null)
const errorMessage = ref('')
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

function getLevelName(level: string) {
    const levels: Record<string, string> = {
        WEAK: '薄弱',
        NORMAL: '一般',
        MASTERED: '已掌握',
    }

    return levels[level] ?? level
}

function getLevelType(
    level: string,
): 'danger' | 'warning' | 'success' {
    if (level === 'WEAK') return 'danger'
    if (level === 'NORMAL') return 'warning'
    return 'success'
}

function getErrorMessage(error: unknown): string {
    return error instanceof Error
        ? error.message
        : '数据加载失败，请稍后重试'
}

// 按掌握度从低到高排序
const sortedTopics = computed(() => {
    return [...(profile.value?.topics ?? [])].sort(
        (a, b) => a.masteryScore - b.masteryScore,
    )
})

// 当前最低掌握度
const weakestTopic = computed(() => {
    return sortedTopics.value[0] ?? null
})

// 五个知识主题的平均掌握度
const averageMastery = computed(() => {
    const topics = profile.value?.topics ?? []

    if (topics.length === 0) return 0

    const total = topics.reduce(
        (sum, item) => sum + item.masteryScore,
        0,
    )

    return Math.round(total / topics.length)
})

// 从真实后端接口加载数据
async function loadResults() {
    loading.value = true
    errorMessage.value = ''
    recommendationError.value = ''
    profile.value = null
    recommendation.value = null

    try {
        profile.value = await getMyProfile()
    } catch (error) {
        errorMessage.value = getErrorMessage(error)
        loading.value = false
        return
    }

    try {
        recommendation.value = await getNextTraining()
    } catch (error) {
        // 推荐接口失败时，仍然展示已获取的学习画像
        recommendationError.value = getErrorMessage(error)
    } finally {
        loading.value = false
    }
}

onMounted(loadResults)
</script>

<template>
<div class="results-page">
<main class="results-content">

<header class="page-header">
    <div>
        <el-tag type="success">LEARNING PROFILE</el-tag>
<h1>我的学习画像</h1>
<p>了解当前知识掌握情况，找到下一步学习方向。</p>
</div>

<el-button @click="router.push('/dashboard')">
    返回 Dashboard
</el-button>
</header>

<!-- 加载状态 -->
<el-card v-if="loading" shadow="never">
    <el-skeleton :rows="7" animated />
</el-card>

<!-- 画像加载失败 -->
<el-card v-else-if="errorMessage" shadow="never">
    <el-alert
title="学习画像加载失败"
:description="errorMessage"
type="warning"
:closable="false"
show-icon
/>

<div class="actions">
    <el-button type="primary" @click="router.push('/diagnosis')">
    前往学习诊断
    </el-button>

    <el-button @click="loadResults">
    重新加载
    </el-button>
    </div>
    </el-card>

    <template v-else-if="profile">

<!-- 总览 -->
<section class="summary-grid">
    <el-card shadow="never">
    <el-statistic
title="已分析知识主题"
:value="profile.topics.length"
suffix="个"
    />
    </el-card>

    <el-card shadow="never">
    <el-statistic
title="当前平均掌握度"
:value="averageMastery"
suffix="%"
    />
    </el-card>
    </section>

    <!-- 五个主题 -->
    <el-card shadow="never" class="section-card">
    <template #header>
    <h2>知识主题掌握度</h2>
    </template>

    <div
v-for="item in sortedTopics"
    :key="item.topic"
class="topic-row"
>
<div class="topic-header">
    <strong>
        {{ item.topicName || topicNames[item.topic] }}
</strong>

<el-tag :type="getLevelType(item.level)">
    {{ getLevelName(item.level) }}
</el-tag>
</div>

<el-progress
:percentage="item.masteryScore"
:stroke-width="12"
    />
    </div>

    <p class="tip">
    掌握度由后端根据学习记录计算，
            不等于某一次诊断的答题正确率。
          </p>
          </el-card>

          <!-- 薄弱主题 -->
          <el-card
v-if="weakestTopic"
    shadow="never"
class="section-card"
    >
    <template #header>
    <h2>当前薄弱知识点</h2>
    </template>

    <h3>
    {{ weakestTopic.topicName }}
</h3>

<p>
当前掌握度：
            <strong>{{ weakestTopic.masteryScore }}%</strong>
</p>

<p>
该主题目前是掌握度最低的主题之一，
            建议优先加强相关练习。
          </p>
          </el-card>

          <!-- 个性化训练推荐 -->
          <el-card shadow="never" class="section-card">
    <template #header>
    <h2>个性化训练推荐</h2>
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

<p class="reason">
    推荐原因：{{ recommendation.reason }}
</p>

<div class="training-preview">
    <h3>推荐练习场景</h3>

    <p>
    {{ recommendation.trainingCase.clauseText }}
</p>

<strong>
{{ recommendation.trainingCase.question }}
</strong>
</div>

<div
v-if="recommendation.learningCard"
    class="learning-card"
    >
    <h3>
        {{ recommendation.learningCard.title }}
</h3>

<p>
{{ recommendation.learningCard.content }}
</p>
</div>

<p class="tip">
    当前展示训练推荐预览，正式答题功能将在训练模块实现。
            </p>
            </template>

            <el-alert
v-else
title="训练推荐暂时不可用"
:description="recommendationError"
type="warning"
:closable="false"
    />
    </el-card>

    <div class="actions">
    <el-button @click="loadResults">
    刷新学习画像
    </el-button>

    <el-button type="primary" @click="router.push('/dashboard')">
    返回学习中心
    </el-button>
    </div>

    </template>

    </main>
    </div>
    </template>

    <style scoped>
.results-page {
    min-height: 100vh;
    background: #f4f7fb;
}

.results-content {
    max-width: 1000px;
    margin: 0 auto;
    padding: 40px 24px 64px;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 20px;
    margin-bottom: 28px;
}

.page-header h1 {
    margin: 14px 0 8px;
    color: #1e293b;
    font-size: 30px;
}

.page-header p,
.tip,
.reason {
    color: #64748b;
    line-height: 1.8;
}

.summary-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 20px;
    margin-bottom: 20px;
}

.section-card {
    margin-bottom: 20px;
    border-radius: 12px;
}

.section-card h2 {
    margin: 0;
    font-size: 19px;
    color: #1e293b;
}

.section-card h3 {
    color: #334155;
}

.topic-row {
    margin-bottom: 26px;
}

.topic-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.topic-header strong {
    color: #334155;
}

.tip {
    font-size: 13px;
    margin-top: 16px;
}

.recommendation-tags {
    display: flex;
    gap: 12px;
    margin-bottom: 18px;
}

.training-preview,
.learning-card {
    padding: 20px;
    background: #f8fafc;
    border: 1px solid #e5eaf0;
    border-radius: 10px;
    margin-top: 20px;
    line-height: 1.8;
    white-space: pre-wrap;
}

.actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    flex-wrap: wrap;
}

@media (max-width: 600px) {
.results-content {
        padding: 24px 16px;
    }

.page-header {
        flex-direction: column;
    }

.summary-grid {
        grid-template-columns: 1fr;
    }
}
</style>
