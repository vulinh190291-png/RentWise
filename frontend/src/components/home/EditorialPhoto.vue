<script setup lang="ts">
import {ref,watch} from 'vue'
const props=defineProps<{src:string;alt:string;width:number;height:number;author:string;source:string;license:string;licenseUrl:string;note:string;priority?:boolean}>()
const failed=ref(false)
watch(()=>props.src,()=>failed.value=false)
</script>
<template><figure><img v-if="!failed" :src="src" :alt="alt" :width="width" :height="height" :loading="priority?'eager':'lazy'" :fetchpriority="priority?'high':'auto'" @error="failed=true"><div v-else class="fallback" role="status">摄影暂时无法加载，文字内容仍可阅读。</div><figcaption><span>{{note}}</span><span><a :href="source" target="_blank" rel="noopener noreferrer">{{author}} · 作品来源 ↗</a> / <a :href="licenseUrl" target="_blank" rel="noopener noreferrer">{{license}} ↗</a></span></figcaption></figure></template>
<style scoped>figure{margin:0;min-width:0}img{display:block;width:100%;height:auto}figcaption{display:flex;flex-wrap:wrap;justify-content:space-between;gap:6px 16px;font-size:10px;line-height:1.8;color:#66645f;padding-top:12px}figcaption a{color:inherit;text-underline-offset:3px}.fallback{min-height:220px;background:#e9e6df;display:grid;place-items:center;padding:24px;font-size:14px}
@media(max-width:767px){
 figcaption{font-size:12px;line-height:1.7;gap:4px;padding-top:10px;overflow-wrap:anywhere}
 figcaption>span{display:block;width:100%}
 figcaption a{display:inline-flex;align-items:center;min-height:44px;max-width:100%}
}
</style>
