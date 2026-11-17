<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { createMapSession, type MapStatus } from '../../data/mapSession'
import { amapFactory } from '../../data/amapAdapter'
const props=defineProps<{cities:{id:string;name:string}[];selectedId:string}>()
const emit=defineEmits<{select:[id:string];fallback:[]}>()
const canvas=ref<HTMLElement>()
const status=ref<MapStatus>('unconfigured')
const session=createMapSession(value=>status.value=value,id=>emit('select',id))
function sync(){session.selectCity(props.selectedId,matchMedia('(prefers-reduced-motion: reduce)').matches)}
async function start(){if(!canvas.value)return;sync();await session.start(amapFactory(canvas.value,props.cities),25000)}
watch(()=>props.selectedId,sync)
onMounted(start)
onBeforeUnmount(session.dispose)
</script>
<template>
 <section aria-label="中国城市地图" class="city-map">
  <div ref="canvas" class="canvas" :aria-hidden="status!=='ready'"></div>
  <p v-if="status==='loading'" role="status">正在连接地图，城市目录仍可使用…</p>
  <div v-if="status==='error'" role="alert"><p>地图暂不可用，请检查代理、平台授权或网络。城市目录与详情仍可使用。</p><button class="rw-button" @click="start">重试地图</button></div>
  <p class="map-caption">八城样本视野 · 可缩放查看密集节点；全部城市均可在下方目录选择。</p>
  <button class="rw-button" @click="emit('fallback')">使用城市列表</button>
 </section>
</template>
<style scoped>.canvas{height:520px;width:100%;background:#eeede7}.city-map{margin-bottom:24px}.city-map p{font-size:14px;line-height:1.8}@media(max-width:767px){.canvas{height:360px;touch-action:pan-y}}</style>
<style>
.map-controls{position:absolute;z-index:20;top:12px;right:12px;display:flex;gap:6px}
.map-controls button{min-height:44px;padding:8px;background:white;color:#242424;border:1px solid #777;border-radius:8px;cursor:pointer;font-size:12px}
.amap-city-button{display:block;width:24px;height:24px;padding:6px;background:transparent;border:0;border-radius:50%;cursor:pointer}
.amap-city-button::after{content:'';display:block;width:12px;height:12px;border:2px solid white;border-radius:50%;background:#68655f;box-shadow:0 0 0 1px #242424}
.amap-city-button[aria-pressed=true]::after{background:#111;box-shadow:0 0 0 3px #b4955f}
.amap-city-button:focus-visible{outline:3px solid #242424;outline-offset:3px}
</style>
