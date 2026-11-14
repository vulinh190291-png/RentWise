<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { housingStories } from '../../data/housingShowcase'
const index = ref(0)
const story = computed(() => housingStories[index.value]!)
const details = ref(false)
const imageFailed = ref(false)
const opener = ref<HTMLButtonElement>()
function move(delta: number) { index.value = (index.value + delta + housingStories.length) % housingStories.length; imageFailed.value = false }
function select(value: number) { index.value = value; imageFailed.value = false }
function restoreFocus() { nextTick(() => opener.value?.focus()) }
</script>

<template>
 <section class="showcase" aria-label="住宅科普轮播" aria-roledescription="轮播">
  <img v-if="!imageFailed" :src="story.image" :alt="story.alt" :width="story.imageWidth" :height="story.imageHeight" :style="{ objectPosition: story.imagePosition }" fetchpriority="high" @error="imageFailed = true" />
  <div class="image-note">{{ imageFailed ? '图片暂不可用 · 仍可阅读科普' : '居住摄影参考 · 非出租房源' }}</div>
  <div class="showcase-content">
   <div aria-live="polite" aria-atomic="true">
    <p class="story-number">0{{ index + 1 }} / 0{{ housingStories.length }} · 居住笔记</p>
    <h2>{{ story.title }}</h2>
    <p class="subtitle">{{ story.subtitle }}</p>
   </div>
   <button ref="opener" class="more" @click="details = true">了解这类空间 <span aria-hidden="true">↗</span></button>
   <div class="controls">
    <div class="dots" aria-label="选择住宅介绍">
     <button v-for="(item, i) in housingStories" :key="item.id" :aria-label="`查看${item.title}`" :aria-pressed="index === i" @click="select(i)"><span /></button>
    </div>
    <div class="arrows"><button aria-label="上一项住宅介绍" @click="move(-1)">←</button><button aria-label="下一项住宅介绍" @click="move(1)">→</button></div>
   </div>
  </div>
 </section>
 <el-dialog v-model="details" :title="story.title" class="housing-dialog" width="min(760px, calc(100% - 32px))" align-center :close-on-click-modal="false" @closed="restoreFocus">
  <img v-if="!imageFailed" :src="story.image" :alt="story.alt" :width="story.imageWidth" :height="story.imageHeight" :style="{ objectPosition: story.imagePosition }" class="detail-image" @error="imageFailed = true" />
  <p class="detail-note">摄影：<a :href="story.source" target="_blank" rel="noopener noreferrer">{{ story.photographer }} / Unsplash<span class="new-tab">（新窗口）</span></a>。图片仅作空间观察参考，不代表可出租房源或房屋法律分类。</p>
  <p class="detail-intro">{{ story.introduction }}</p>
  <div class="detail-columns"><section><h3>可能适合你的地方</h3><ul><li v-for="text in story.advantages" :key="text">{{ text }}</li></ul></section><section><h3>也需要留意</h3><ul><li v-for="text in story.considerations" :key="text">{{ text }}</li></ul></section></div>
  <section><h3>入住前，逐项核对</h3><ul><li v-for="text in story.checklist" :key="text">{{ text }}</li></ul></section>
  <template #footer><button class="rw-button" @click="details = false">关闭详情</button></template>
 </el-dialog>
</template>

<style scoped>
.showcase { position:relative; min-height:480px; overflow:hidden; background:#c5bcae; color:white; }
.showcase > img { position:absolute; width:100%; height:100%; object-fit:cover; }
.showcase::after { content:''; position:absolute; inset:0; background:linear-gradient(180deg,#181a1700 24%,#1d1d1f18 40%,#1d1d1fb8 70%,#1d1d1ff2 100%); pointer-events:none; }
.image-note { position:absolute; z-index:1; top:20px; left:24px; font-size:11px; color:#272725; background:#ffffffe0; padding:7px 11px; border-radius:999px; }
.showcase-content { position:relative; z-index:1; display:flex; flex-direction:column; justify-content:flex-end; height:100%; min-height:480px; padding:28px 32px; }
.rw-kicker { margin:0 0 40px; color:#fff; }
.story-number { margin:0; font-size:11px; letter-spacing:.08em; color:#eeeae2; }
h2 { font-size:clamp(30px,3vw,42px); font-weight:500; letter-spacing:-.04em; margin:8px 0; }
.subtitle { font-size:14px; line-height:1.8; margin:0; }
.more { align-self:flex-start; margin:12px 0 14px; color:white; border:0; border-bottom:1px solid #ffffff90; background:none; min-height:44px; padding:8px 0; cursor:pointer; }
.more span { margin-left:32px; }
.controls { display:flex; justify-content:space-between; align-items:center; gap:16px; }
.dots,.arrows { display:flex; gap:4px; }
.controls button { min-width:44px; height:44px; border:0; background:transparent; color:white; cursor:pointer; border-radius:50%; }
.dots span { display:block; width:6px; height:6px; border-radius:50%; background:#ffffff80; margin:auto; }
.dots [aria-pressed=true] span { width:20px; border-radius:4px; background:white; }
.arrows button { border:1px solid #ffffff60; font-size:20px; }
.controls button:hover { background:#ffffff20; }
.detail-image { width:100%; max-height:320px; height:auto; object-fit:contain; border-radius:12px; }
.new-tab { font-size:11px; }.detail-note a { color:inherit; text-underline-offset:3px; }.detail-note { font-size:12px; color:var(--rw-muted); line-height:1.7; }
.detail-intro { line-height:1.9; }.detail-columns { display:grid; grid-template-columns:1fr 1fr; gap:24px; }
li { margin-bottom:10px; line-height:1.8; } ul { padding-left:20px; } h3 { color:var(--rw-ink); font-size:16px; }
@media(max-width:1023px) { .showcase,.showcase-content { min-height:380px; } }
@media(max-width:767px) { .showcase,.showcase-content { min-height:320px; } .showcase-content { padding:22px; } h2 { font-size:28px; } .subtitle { font-size:12px; } .image-note { top:16px; left:20px; font-size:10px; } .rw-kicker { margin-bottom:24px; } .more { margin:8px 0; } .detail-columns { grid-template-columns:1fr; gap:0; } }
</style>
