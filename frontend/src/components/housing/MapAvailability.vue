<script setup lang="ts">
import type { MapStatus } from '../../data/mapSession'
defineProps<{ status: MapStatus }>()
defineEmits<{ close: []; retry: [] }>()
</script>

<template>
 <section id="map-notice" class="map-availability" aria-labelledby="map-title">
  <template v-if="status==='unconfigured'"><h3 id="map-title">地图尚未开放</h3><p>本版先提供城市列表与居住笔记。地图服务授权与接入配置尚未确认，当前不加载地图服务、不展示地理轮廓或城市坐标。</p></template>
  <template v-else-if="status==='loading'"><h3 id="map-title">正在连接地图</h3><p role="status">城市列表与详情仍可继续使用。</p></template>
  <template v-else-if="status==='error'"><h3 id="map-title">地图暂不可用</h3><p role="alert">地图连接失败，请继续使用城市列表与详情。</p><button class="rw-button" @click="$emit('retry')">重试地图</button></template>
  <template v-else><h3 id="map-title">地图已连接</h3><p>也可使用城市列表进行键盘选择。</p></template>
  <button class="rw-button" @click="$emit('close')">返回城市浏览</button>
 </section>
</template>

<style scoped>
.map-availability { border:1px solid var(--rw-line); padding:24px; border-radius:16px; margin-bottom:24px; }
h3 { font-size:16px; margin:0 0 12px; }p { line-height:1.8; max-width:680px; }.rw-button { margin:8px 12px 0 0; }
</style>
