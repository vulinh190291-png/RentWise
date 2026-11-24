<script setup lang="ts">
import {computed,ref,watch,nextTick} from 'vue'
import {RouterLink,useRoute} from 'vue-router'
import {livingArticles,livingPhoto} from '../../data/living'
import EditorialPhoto from '../../components/home/EditorialPhoto.vue'
import './living.css'
const route=useRoute()
const selectedArticle=computed(()=>livingArticles.find(a=>a.slug===route.params.slug))
const selectedPhoto=computed(()=>selectedArticle.value?livingPhoto(selectedArticle.value):undefined)
const article=computed(()=>selectedArticle.value ?? livingArticles[0]!)
const photo=computed(()=>selectedPhoto.value ?? livingPhoto(livingArticles[0]!)!)
const title=ref<HTMLElement>()
watch(()=>route.params.slug,async()=>{await nextTick();title.value?.focus()})
</script>
<template>
<main class="living-page">
<nav aria-label="专题导航">
<RouterLink to="/living">← 返回 Living 目录</RouterLink>
<RouterLink to="/">首页</RouterLink>
</nav>
<template v-if="selectedArticle">
<header>
<p class="kicker">LIVING / {{article.category}} / 编辑示例</p>
<h1 ref="title" tabindex="-1">{{article.title}}</h1>
<p class="deck">{{article.deck}}</p>
<small>{{article.author}} · 更新 {{article.updated}}</small>
</header>
<EditorialPhoto v-if="selectedPhoto" :key="article.slug" :src="photo.image" :alt="photo.alt" :width="photo.imageWidth" :height="photo.imageHeight" :author="photo.photographer" :source="photo.source" license="Unsplash License" license-url="https://unsplash.com/license" note="原图等比展示；灵感参考，不是商品或房源" priority/>
<div v-if="!selectedPhoto" class="living-placeholder" role="status">专题摄影待补充<br>
<small>尚无经授权的对应摄影，此处为文字占位。</small>
</div>
<div class="living-reading">
<section v-for="section in article.sections" :key="section.title">
<h2>{{section.title}}</h2>
<p>{{section.text}}</p>
</section>
<section>
<h2>可以从这些小事开始</h2>
<ul>
<li v-for="tip in article.tips" :key="tip">{{tip}}</li>
</ul>
</section>
<section>
<h2>署名、来源与阅读边界</h2>
<p>本文由 {{article.author}} 撰写，为原创编辑建议，不是专业检测、效果保证或商品测评。</p>
<ul>
<li v-for="source in article.sources" :key="source.title">
<a v-if="source.url" :href="source.url" target="_blank" rel="noopener noreferrer">{{source.title}}</a>
<span v-else>{{source.title}}</span>
</li>
</ul>
<p>摄影作者、作品与许可随图展示；未改变图片原有许可。</p>
</section>
</div>
<footer>
<RouterLink to="/living">浏览全部专题</RouterLink>
<RouterLink v-for="other in livingArticles.filter(a=>a.slug!==article?.slug)" :key="other.slug" :to="'/living/'+other.slug">{{other.title}} →</RouterLink>
</footer>
</template>
<section v-if="!selectedArticle" role="status">
<h1>专题尚未发布</h1>
<p>此地址没有对应文章，请返回目录选择已有专题。</p>
<RouterLink to="/living">返回 Living 目录</RouterLink>
</section>
</main>
</template>
