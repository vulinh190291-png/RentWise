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
  <img v-if="!imageFailed" :src="story.image" :alt="story.alt" width="1200" height="1000" fetchpriority="high" @error="imageFailed = true" />
  <div class="image-note">{{ imageFailed ? '图片暂不可用 · 仍可阅读科普' : '空间示意占位 · 非实景摄影' }}</div>
  <div class="showcase-content">
   <p class="rw-kicker">A PLACE TO UNDERSTAND</p>
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
  <img v-if="!imageFailed" :src="story.image" :alt="story.alt" width="1200" height="1000" class="detail-image" @error="imageFailed = true" />
  <p class="detail-note">空间示意占位，待替换为具备使用权的摄影。本文为一般性居住知识，不是房源介绍。</p>
  <p class="detail-intro">{{ story.introduction }}</p>
  <div class="detail-columns"><section><h3>可能适合你的地方</h3><ul><li v-for="text in story.advantages" :key="text">{{ text }}</li></ul></section><section><h3>也需要留意</h3><ul><li v-for="text in story.considerations" :key="text">{{ text }}</li></ul></section></div>
  <section><h3>入住前，逐项核对</h3><ul><li v-for="text in story.checklist" :key="text">{{ text }}</li></ul></section>
  <template #footer><button class="rw-button" @click="details = false">关闭详情</button></template>
 </el-dialog>
</template>

<style scoped>
.showcase { position:relative; min-height:620px; overflow:hidden; background:#c5bcae; color:white; }
.showcase > img { position:absolute; width:100%; height:100%; object-fit:cover; }
.showcase::after { content:''; position:absolute; inset:0; background:linear-gradient(180deg,#181a1700 15%,#24251f30 40%,#25251fe8 100%); }
.image-note { position:absolute; z-index:1; top:24px; left:28px; font-size:11px; color:#272725; background:#ffffffe0; padding:8px 12px; border-radius:999px; }
.showcase-content { position:relative; z-index:1; display:flex; flex-direction:column; justify-content:flex-end; min-height:620px; padding:40px; }
.rw-kicker { margin:0 0 40px; color:#fff; }
.story-number { font-size:12px; letter-spacing:.08em; color:#eeeae2; }
h2 { font-size:clamp(32px,3.4vw,48px); font-weight:500; letter-spacing:-.04em; margin:12px 0; }
.subtitle { font-size:15px; line-height:1.8; margin:0; }
.more { align-self:flex-start; margin:24px 0 30px; color:white; border:0; border-bottom:1px solid #ffffff90; background:none; padding:8px 0; cursor:pointer; }
.more span { margin-left:32px; }
.controls { display:flex; justify-content:space-between; align-items:center; gap:16px; }
.dots,.arrows { display:flex; gap:4px; }
.controls button { min-width:44px; height:44px; border:0; background:transparent; color:white; cursor:pointer; border-radius:50%; }
.dots span { display:block; width:6px; height:6px; border-radius:50%; background:#ffffff80; margin:auto; }
.dots [aria-pressed=true] span { width:20px; border-radius:4px; background:white; }
.arrows button { border:1px solid #ffffff60; font-size:20px; }
.controls button:hover { background:#ffffff20; }
.detail-image { width:100%; height:200px; object-fit:cover; border-radius:12px; }
.detail-note { font-size:12px; color:var(--rw-muted); line-height:1.7; }
.detail-intro { line-height:1.9; }.detail-columns { display:grid; grid-template-columns:1fr 1fr; gap:24px; }
li { margin-bottom:10px; line-height:1.8; } ul { padding-left:20px; } h3 { color:var(--rw-ink); font-size:16px; }
@media(max-width:1023px) { .showcase,.showcase-content { min-height:460px; } }
@media(max-width:767px) { .showcase,.showcase-content { min-height:380px; } .showcase-content { padding:24px; } .rw-kicker { margin-bottom:24px; } .more { margin:12px 0; } .detail-columns { grid-template-columns:1fr; gap:0; } }
</style>
