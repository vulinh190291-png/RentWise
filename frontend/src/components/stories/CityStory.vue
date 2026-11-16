<script setup lang="ts">
import {ref,watch} from 'vue'
import type {CityStory} from '../../data/cityStories'
const props=defineProps<{story:CityStory}>()
const failed=ref(false)
watch(()=>props.story.id,()=>failed.value=false)
</script>
<template>
 <article class="story">
  <header><p class="eyebrow">RENTWISE / 居住文化笔记 · {{story.city}}</p><h1 tabindex="-1" id="story-title">{{story.title}}</h1><p class="deck">{{story.deck}}</p><small>内容样本 · Draft · 核验 {{story.updated}}</small></header>
  <figure :class="{portrait:story.image && story.image.height>story.image.width && !failed}">
   <img v-if="story.image && !failed" :src="story.image.src" :alt="story.image.alt" :width="story.image.width" :height="story.image.height" @error="failed=true" />
   <div v-else class="photo-pending" role="status"><span>{{story.city}} / 空间影像</span><strong>{{failed?'摄影暂时无法加载':'城市摄影待补充'}}</strong><p>{{failed?'文字与资料来源仍可完整阅读。':'尚未完成本城摄影的逐图授权核验，此处不展示替代实景。'}}</p></div>
   <figcaption v-if="story.image"><p class="photo-note">{{story.image.caption}}</p><a :href="story.image.work" target="_blank" rel="noopener noreferrer">摄影：{{story.image.author}} · 作品原页（新窗口）</a> · <a :href="story.image.licenseUrl" target="_blank" rel="noopener noreferrer">{{story.image.license}}</a><br>{{story.image.changes}}</figcaption>
   <figcaption v-else>图片作者、作品地址与许可：待核验，未采用外部摄影。</figcaption>
  </figure>
  <div class="reading"><section v-for="(section,i) in story.sections" :key="section.title"><p class="eyebrow">0{{i+1}} / {{section.source<0?'编辑观察':'空间与文化'}}</p><h2>{{section.title}}</h2><p>{{section.text}}</p><a v-if="section.source>=0" :href="story.sources[section.source]?.url" target="_blank" rel="noopener noreferrer">依据：{{story.sources[section.source]?.title}} ↗</a></section>
  <aside><h2>带着问题，观察居所</h2><p>以下为 RentWise 编辑建议，非房屋检测、风险统计或法律意见。</p><ul><li v-for="item in story.observations" :key="item">{{item}}</li></ul></aside>
  <section class="story-sources"><h2>资料来源与阅读边界</h2><p>历史样本不代表全城居民；没有提供房价、租金或房源。核验日期 {{story.updated}}，不等于原文发布日期。</p><ol><li v-for="source in story.sources" :key="source.url"><a :href="source.url" target="_blank" rel="noopener noreferrer">{{source.title}}（新窗口）</a></li></ol></section></div>
 </article>
</template>
<style scoped>
.story{color:#252523}header{max-width:850px;padding:48px 0 36px}.eyebrow{font-size:11px;letter-spacing:.16em;color:#68665f}h1{font-size:clamp(32px,4vw,58px);line-height:1.2;letter-spacing:-.04em;font-weight:550;margin:22px 0}.deck{font-size:18px;line-height:1.9;color:#55534d}small,figcaption{font-size:12px;line-height:1.8;color:#68665f}figure{margin:0}img{display:block;width:100%;height:auto;object-fit:contain}.portrait{display:grid;grid-template-columns:minmax(0,1.15fr) minmax(0,1fr);gap:40px;max-width:850px;align-items:center}.portrait img{max-width:440px}.photo-note{font-size:16px;line-height:1.9;margin:0 0 20px}.photo-pending{min-height:320px;display:flex;flex-direction:column;justify-content:center;padding:36px;background:#e9e6df}.photo-pending span{font-size:12px;letter-spacing:.14em}.photo-pending strong{font-size:30px;font-weight:450;margin:22px 0}.photo-pending p{max-width:480px;line-height:1.8}figcaption{padding:16px 0;border-bottom:1px solid #d8d5cd}a{color:inherit;text-underline-offset:4px}.reading{max-width:720px;margin:0 auto}section{padding:36px 0;border-bottom:1px solid #d8d5cd}h2{font-weight:500;font-size:28px;line-height:1.4}section p,li,aside p{line-height:1.95;font-size:16px}section>a{font-size:12px}aside{margin:32px 0;padding:28px;background:#efede6}li{margin:12px 0}ol,ul{padding-left:22px}.story-sources a{overflow-wrap:anywhere}a:focus-visible{outline:2px solid #333;outline-offset:5px}@media(max-width:767px){header{padding:28px 0}.deck{font-size:16px}.portrait{display:block}.portrait img{max-width:none}.portrait figcaption{padding-top:16px}.photo-pending{padding:24px;min-height:260px}h2{font-size:24px}aside{padding:20px}}
</style>
