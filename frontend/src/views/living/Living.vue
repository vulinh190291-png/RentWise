<script setup lang="ts">
import {computed,ref} from 'vue'
import {RouterLink} from 'vue-router'
import {livingArticles,livingCategories,livingPhoto,type LivingCategory} from '../../data/living'
import EditorialPhoto from '../../components/home/EditorialPhoto.vue'
import './living.css'
const category=ref<LivingCategory|'全部'>('全部')
const articles=computed(()=>livingArticles.filter(a=>category.value==='全部'||a.category===category.value))
const featured=computed(()=>livingArticles.find(a=>a.featured)!)
const photo=computed(()=>livingPhoto(featured.value)!)
</script>
<template><main class="living-page"><nav aria-label="Living导航"><RouterLink to="/">RentWise / 首页</RouterLink><RouterLink to="/explore">Explore</RouterLink><RouterLink to="/dashboard">Learn</RouterLink></nav>
<header><p class="kicker">LIVING / A JOURNAL OF EVERYDAY LIFE</p><h1>让居住，回到生活本身。</h1><p class="deck">观察空间，记录日常，重新认识身边的物件。</p></header>
<section class="living-feature" aria-label="精选专题"><EditorialPhoto :src="photo.image" :alt="photo.alt" :width="photo.imageWidth" :height="photo.imageHeight" :author="photo.photographer" :source="photo.source" license="Unsplash License" license-url="https://unsplash.com/license" note="空间参考摄影；非房源或商品" priority/><div><p class="kicker">本期精选 / {{featured.category}}</p><h2><RouterLink :to="'/living/'+featured.slug">{{featured.title}} ↗</RouterLink></h2><p>{{featured.deck}}</p><small>编辑示例 · {{featured.updated}}</small></div></section>
<section class="living-directory" aria-labelledby="directory-title"><h2 id="directory-title">慢慢读，慢慢发现。</h2><div class="living-filters" role="group" aria-label="专题分类"><button v-for="item in ['全部',...livingCategories] as const" :key="item" :aria-pressed="category===item" @click="category=item">{{item}}</button></div><p role="status">{{category}} · {{articles.length}} 篇专题</p><article v-for="(article,index) in articles" :key="article.slug" class="living-row"><span class="kicker">0{{index+1}} / {{article.category}}</span><div><h3><RouterLink :to="'/living/'+article.slug">{{article.title}} ↗</RouterLink></h3><p>{{article.deck}}</p></div><small>编辑示例<br>{{article.updated}}</small></article><p v-if="!articles.length">该分类暂无专题，请选择其他分类。</p></section><footer>RentWise 编辑内容示例。没有房源、商品购买或个性化推荐服务。<RouterLink to="/">返回首页</RouterLink></footer></main></template>
