<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { loadCities, matchesCity, type ExploreCity } from '../../data/explore'

const cities = ref<ExploreCity[]>([])
const query = ref('')
const loading = ref(true)
const failed = ref(false)
const selectedId = ref('')
const detailOpen = ref(false)
const mapNotice = ref(false)
const imageFailed = ref(false)
const search = ref<HTMLInputElement>()
const title = ref<HTMLElement>()
const mapButton = ref<HTMLButtonElement>()
let opener: HTMLButtonElement | undefined
let request: AbortController | undefined
const filtered = computed(() => cities.value.filter(city => matchesCity(city, query.value)))
const selected = computed(() => filtered.value.find(city => city.id === selectedId.value) ?? filtered.value[0])
watch(query, () => { detailOpen.value = false })
async function load() {
  request?.abort()
  const active = new AbortController(); request = active
  loading.value = true; failed.value = false; detailOpen.value = false
  const timeout = window.setTimeout(() => { if (request === active) { failed.value = true; loading.value = false; active.abort() } }, 12000)
  try { cities.value = await loadCities(active.signal) }
  catch { if (!active.signal.aborted) failed.value = true }
  finally { window.clearTimeout(timeout); if (!active.signal.aborted) loading.value = false }
}
async function select(city: ExploreCity, event: MouseEvent) {
  opener = event.currentTarget as HTMLButtonElement
  selectedId.value = city.id; detailOpen.value = true
  await nextTick(); title.value?.focus()
}
async function back() {
  detailOpen.value = false
  await nextTick()
  if (opener?.isConnected) opener.focus(); else search.value?.focus()
}
async function closeMap() { mapNotice.value = false; await nextTick(); mapButton.value?.focus() }
async function clearSearch() { query.value = ''; await nextTick(); search.value?.focus() }
onMounted(load)
onBeforeUnmount(() => request?.abort())
</script>

<template>
 <div class="explore" @keydown.esc="mapNotice ? closeMap() : back()">
  <a class="skip" href="#city-search">跳至城市搜索</a>
  <header class="rw-container topbar"><RouterLink class="brand" to="/">RentWise<span>居住有知，选择有底。</span></RouterLink><nav aria-label="Explore 导航"><RouterLink to="/explore" aria-current="page">城市探索</RouterLink><RouterLink to="/dashboard">风险学习 ↗</RouterLink></nav></header>
  <main class="rw-container">
   <section class="intro" aria-labelledby="explore-title"><div><p class="rw-kicker">EXPLORE / 中国城市居住笔记</p><h1 id="explore-title"><span>从一座城，</span><span>读懂一种生活。</span></h1><p>从院落、街巷到日常空间，探索不同的居住方式。<br class="desktop-break" />先认识一座城，再把观察带进自己的租住选择。</p></div><figure v-if="!imageFailed"><img src="/images/housing-courtyard.webp" width="1400" height="933" alt="庭院空间摄影参考，不对应本页城市" @error="imageFailed=true" /><figcaption>空间观察参考 · 非城市实景<br /><a href="https://unsplash.com/photos/uj3VsJLhWE4" target="_blank" rel="noopener noreferrer">Erwan Hesry / Unsplash（新窗口）</a></figcaption></figure></section>
   <div class="section-heading"><div><p class="rw-kicker">01 / CITY NOTEBOOK</p><h2>城市不同，日常各有答案。</h2></div><button ref="mapButton" class="text-button" :aria-expanded="mapNotice" aria-controls="map-notice" @click="mapNotice=!mapNotice">地图接入说明</button></div>
   <section v-if="mapNotice" id="map-notice" class="notice" aria-labelledby="map-title"><h3 id="map-title">地图尚未开放</h3><p>本版先提供城市列表与居住笔记。待地图服务授权与接入配置确认后，再开放地图浏览；当前不展示地理轮廓或城市坐标。</p><button class="rw-button" @click="closeMap">返回城市浏览</button></section>
   <div class="workspace" :class="{ 'detail-open': detailOpen }" :aria-busy="loading">
    <section class="city-browser" aria-label="城市浏览">
     <label for="city-search">搜索城市、拼音或住宅特点</label><div class="search-row"><input id="city-search" ref="search" v-model="query" type="search" placeholder="试试：上海、Suzhou、院落" autocomplete="off" /><button v-if="query" class="clear" @click="clearSearch">清空搜索</button></div>
     <p v-if="loading" role="status">正在加载城市资料…</p>
     <div v-else-if="failed" role="alert" class="state"><h3>城市资料暂不可用</h3><p>请检查网络后重试。</p><button class="rw-button" @click="load">重新加载</button></div>
     <template v-else><p class="count" role="status">{{ filtered.length }} 座城市 · 内容样本，不是完整城市名录</p>
      <div v-if="!filtered.length" class="state"><h3>{{ cities.length ? '暂未找到匹配城市' : '城市资料正在整理' }}</h3><p>{{ cities.length ? '换个城市名、拼音或住宅关键词试试。' : '稍后再来看看。' }}</p></div>
      <ul v-else class="city-list"><li v-for="(city, index) in filtered" :key="city.id"><button class="city-choice" :aria-pressed="selected?.id===city.id" aria-controls="city-detail" @click="select(city,$event)"><span class="number">{{ String(index+1).padStart(2,'0') }}</span><span><strong>{{ city.name }}</strong><small>{{ city.pinyin }} · {{ city.tag }}</small></span><span aria-hidden="true">↗</span></button></li></ul>
     </template>
    </section>
    <article v-if="!loading && !failed && selected" id="city-detail" class="city-detail" aria-labelledby="city-title">
     <button class="text-button back" @click="back">返回城市列表</button>
     <div class="detail-heading"><p class="rw-kicker">{{ selected.pinyin }} / 居住文化样本</p><h2 id="city-title" ref="title" tabindex="-1">{{ selected.name }}</h2><p>{{ selected.tag }}</p></div>
     <div class="facts"><section><h3>地理与气候</h3><p>{{ selected.geography }}</p><p>{{ selected.climate }}</p><small>定性背景，不是实时天气或具体房屋环境评估。</small></section><section><h3>居住文化</h3><p>{{ selected.culture }}</p></section></div>
     <section class="housing"><p class="rw-kicker">空间样本 / HISTORICAL CONTEXT</p><h3>{{ selected.housingTitle }}</h3><p>{{ selected.housing }}</p></section>
     <section class="risk"><h3>把观察，带进租住选择。</h3><p>{{ selected.risk }}</p><small>RentWise 编辑提出的核对问题，非当地风险统计或法律意见。</small><RouterLink to="/dashboard">开始风险学习 <span aria-hidden="true">↗</span></RouterLink></section>
     <details class="sources"><summary>资料来源与范围 · 核验 {{ selected.updatedAt }}</summary><p>内容为来源事实的简短整理。历史案例不代表全市当代住宅；未提供城市分级、房价、租金或出租房源。</p><ul><li v-for="source in selected.sources" :key="source.url"><a :href="source.url" target="_blank" rel="noopener noreferrer">{{ source.title }}（新窗口）</a><small>核验：{{ source.checkedAt }}</small></li></ul></details>
    </article>
   </div>
  </main>
  <footer class="rw-container"><strong>RentWise Explore</strong><p>居住知识探索 · 不提供房源搜索或交易服务</p><span>内容样本 0.1</span></footer>
 </div>
</template>

<style scoped>
.explore { min-height:100vh; background:#f7f6f2; color:var(--rw-ink); padding-bottom:24px; }
a { color:inherit; text-underline-offset:5px; }.skip { position:absolute; top:-100px; left:16px; background:white; padding:16px; z-index:10; }.skip:focus { top:16px; }
.topbar { min-height:80px; display:flex; align-items:center; justify-content:space-between; gap:20px; border-bottom:1px solid var(--rw-line); }.brand { font-size:24px; font-weight:650; text-decoration:none; letter-spacing:-1px; }.brand span { display:block; font-size:10px; font-weight:400; letter-spacing:.1em; margin-top:4px; color:var(--rw-muted); }nav { display:flex; gap:28px; font-size:13px; }nav a { padding-block:14px; text-decoration:none; }
.intro { display:grid; grid-template-columns:1.5fr 1fr; gap:48px; align-items:center; padding:36px 0; }.intro h1 { font-size:clamp(28px,3.1vw,44px); font-weight:550; letter-spacing:-.05em; line-height:1.3; margin:20px 0; }.intro p:not(.rw-kicker) { font-size:14px; line-height:1.9; color:var(--rw-muted); }.intro figure { margin:0; position:relative; height:210px; border-radius:18px; overflow:hidden; background:var(--rw-sand); }.intro img { width:100%; height:100%; object-fit:cover; }.intro figcaption { position:absolute; bottom:0; left:0; right:0; padding:28px 16px 14px; background:linear-gradient(transparent,#161616d9); color:white; font-size:10px; line-height:1.6; }
.section-heading { display:flex; justify-content:space-between; align-items:center; gap:20px; padding:16px 0 24px; }.section-heading h2 { margin:8px 0 0; font-size:22px; font-weight:500; }.rw-kicker { color:var(--rw-muted); }.text-button { border:0; background:none; text-decoration:underline; text-underline-offset:5px; min-height:44px; padding:10px 4px; cursor:pointer; color:inherit; font-size:13px; }.notice { border:1px solid var(--rw-line); padding:24px; border-radius:16px; margin-bottom:24px; }.notice p { line-height:1.8; max-width:680px; }
.workspace { display:grid; grid-template-columns:minmax(280px,.85fr) minmax(0,1.6fr); border:1px solid var(--rw-line); border-radius:24px; background:#fff; overflow:hidden; }.city-browser { padding:28px; background:#f0efe9; }.city-browser label { font-size:13px; display:block; margin-bottom:12px; }.search-row { position:relative; }input { width:100%; min-height:48px; padding:12px; border:1px solid #aaa99f; border-radius:8px; background:white; color:var(--rw-ink); } .clear { display:block; border:0; background:none; text-decoration:underline; padding:10px 0; min-height:44px; cursor:pointer; }.count { font-size:11px; color:var(--rw-muted); line-height:1.7; margin:18px 0; }.city-list { list-style:none; margin:0; padding:0; }.city-choice { width:100%; display:flex; align-items:center; gap:16px; text-align:left; padding:17px 12px; background:none; border:0; border-top:1px solid #d9d7cf; color:inherit; cursor:pointer; border-radius:4px; }.city-choice:hover { background:#e6e4db; }.city-choice[aria-pressed=true] { background:#252523; color:white; border-color:transparent; }.city-choice > span:nth-child(2) { flex:1; }.city-choice strong { font-size:19px; font-weight:500; }.city-choice small { display:block; font-size:11px; line-height:1.6; margin-top:6px; }.number { font-size:11px; opacity:.8; }
.city-detail { padding:28px 40px 36px; min-width:0; }.back { margin-bottom:8px; }.detail-heading { border-bottom:1px solid var(--rw-line); padding:4px 0 20px; }.detail-heading h2 { font-size:48px; letter-spacing:-.04em; font-weight:500; margin:12px 0; scroll-margin-top:24px; }.detail-heading > p:last-child { font-size:14px; color:var(--rw-muted); }.facts { display:grid; grid-template-columns:1fr 1fr; gap:28px; padding:24px 0; }h3 { font-size:16px; font-weight:600; margin:0 0 12px; }.city-detail p { font-size:14px; line-height:1.85; }.city-detail small { display:block; font-size:11px; color:var(--rw-muted); line-height:1.7; }.housing { border-top:1px solid var(--rw-line); padding:20px 0; }.housing h3 { font-size:24px; font-weight:500; }.risk { background:#f3f1eb; border-radius:14px; padding:24px; }.risk a { display:inline-block; margin-top:18px; font-size:13px; padding:10px 0; }.sources { border-top:1px solid var(--rw-line); margin-top:24px; padding-top:12px; font-size:12px; }.sources summary { padding-block:12px; cursor:pointer; line-height:1.7; }.sources p { font-size:12px; }.sources li { margin:14px 0; overflow-wrap:anywhere; }.sources ul { padding-left:18px; }.state { padding:24px 0; }.state p { font-size:14px; line-height:1.8; }
footer { display:flex; flex-wrap:wrap; align-items:center; justify-content:space-between; gap:16px; padding-top:32px; font-size:11px; color:var(--rw-muted); }footer strong { font-size:16px; color:var(--rw-ink); }
@media(max-width:1100px) { .intro { gap:24px; }.city-browser { padding:20px; }.city-detail { padding:24px; }.facts { gap:18px; }.section-heading h2 { font-size:20px; } }
@media(max-width:767px) { .topbar { min-height:68px; }.brand span { display:none; }nav { gap:16px; font-size:12px; }.intro { display:block; padding:24px 0; }.intro h1 { font-size:28px; }.intro h1 span { display:block; }.intro figure { height:150px; margin-top:20px; }.desktop-break { display:none; }.section-heading { align-items:flex-start; flex-direction:column; gap:4px; padding-top:0; }.workspace { display:block; border-radius:16px; }.city-detail { display:none; padding:20px; }.detail-open .city-browser { display:none; }.detail-open .city-detail { display:block; }.facts { grid-template-columns:1fr; gap:20px; }.detail-heading h2 { font-size:40px; }.risk { padding:20px; }footer { gap:8px; }footer p { width:100%; margin:0; }.city-choice { padding:18px 10px; } }
</style>
