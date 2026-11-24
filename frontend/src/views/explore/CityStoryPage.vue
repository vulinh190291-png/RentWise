<script setup lang="ts">
import {computed,nextTick,watch} from 'vue'
import {RouterLink,useRoute} from 'vue-router'
import CityStory from '../../components/stories/CityStory.vue'
import {exploreNavigation} from '../../data/exploreNavigation'
import {cityStories} from '../../data/cityStories'
const route=useRoute()
const index=computed(()=>cityStories.findIndex(s=>s.id===route.params.cityId))
const story=computed(()=>cityStories[index.value])
const previous=computed(()=>cityStories[index.value-1])
const next=computed(()=>cityStories[index.value+1])
watch(()=>route.params.cityId,async()=>{await nextTick();document.querySelector<HTMLElement>('#story-title')?.focus()})
</script>
<template><main class="story-page"><nav class="top" aria-label="故事导航"><RouterLink to="/explore">← {{exploreNavigation.map ? '返回地图与城市目录' : '返回城市目录'}}</RouterLink><span>RentWise / 城市故事</span></nav><CityStory v-if="story" :story="story"/><section v-else role="status"><h1>故事尚未发布</h1><p>请返回城市目录，其他五座城市的原有资料仍可阅读。</p></section><nav v-if="story" class="sequence" aria-label="篇章导航"><RouterLink v-if="previous" :to="`/explore/stories/${previous.id}`">上一篇 · {{previous.city}}</RouterLink><RouterLink v-if="next" :to="`/explore/stories/${next.id}`">下一篇 · {{next.city}}</RouterLink><RouterLink to="/explore">返回城市目录</RouterLink></nav></main></template>
<style scoped>.story-page{max-width:1120px;margin:auto;padding:24px 32px 60px;color:#252523;background:#f7f6f2;min-height:100vh}.top,.sequence{display:flex;justify-content:space-between;flex-wrap:wrap;gap:18px;padding:16px 0;border-bottom:1px solid #d8d5cd;font-size:14px}.sequence{margin-top:30px}.top a,.sequence a{color:inherit;padding:12px 0;text-underline-offset:5px}.top span{padding:12px 0;color:#68665f}a:focus-visible{outline:2px solid #333;outline-offset:4px}@media(max-width:767px){.story-page{padding:16px 20px 40px}.top span{display:none}}</style>
