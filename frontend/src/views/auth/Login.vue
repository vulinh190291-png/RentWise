
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import HousingShowcase from '../../components/housing/HousingShowcase.vue'
import { isAxiosError } from 'axios'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const submitting = ref(false)
const loginError = ref('')
const formRef = ref<FormInstance>()

const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
}

async function handleLogin() {
  if (!formRef.value || submitting.value) return
  submitting.value = true
  loginError.value = ''

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) { submitting.value = false; return }

  try {
    await authStore.login({
      username: form.username,
      password: form.password,
    })

    ElMessage.success('登录成功，欢迎回来！')
    await router.push('/dashboard')
  } catch (error) {
    const message =
        isAxiosError(error) && typeof error.response?.data?.message === 'string'
          ? error.response.data.message
          : error instanceof Error ? error.message : '登录失败，请检查账号密码'
    loginError.value = message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
 <div class="rw-auth landing">
  <a class="skip" href="#login-form">跳至登录表单</a>
  <header class="rw-container nav"><a class="wordmark" href="#">RentWise<span>居住有知，选择有底。</span></a><nav aria-label="主导航"><a href="#living-notes">居住知识</a><a href="#risk-learning">风险学习</a><RouterLink to="/register">创建账户 <span aria-hidden="true">↗</span></RouterLink></nav></header>
  <main class="rw-container">
   <div class="intro"><p class="rw-kicker">LIVE WITH A LITTLE MORE CLARITY</p><h1>看见生活的可能。<br /><span>也看清每一次选择。</span></h1><p>从认识一处空间，到读懂一份租约。让下一次租住，多一分从容。</p></div>
   <div class="landing-panel">
    <HousingShowcase />
    <section id="login-form" class="login-side" aria-labelledby="login-title">
     <p class="rw-kicker">YOUR NEXT CHAPTER</p><h2 id="login-title">欢迎回来。</h2><p class="login-intro">回到你的学习空间，<br />把未知，变成心中有数。</p>
     <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="submitting" @submit.prevent="handleLogin">
      <el-form-item label="用户名" prop="username"><el-input id="username" v-model="form.username" autocomplete="username" placeholder="输入用户名" size="large" :prefix-icon="User" /></el-form-item>
      <el-form-item label="密码" prop="password"><el-input id="password" v-model="form.password" type="password" autocomplete="current-password" placeholder="输入密码" size="large" show-password :prefix-icon="Lock" /></el-form-item>
      <p v-if="loginError" class="login-error" role="alert">{{ loginError }}</p>
      <el-button type="primary" native-type="submit" class="login-button" :loading="submitting" :disabled="submitting">{{ submitting ? '正在登录' : '登录，继续学习' }}<span v-if="!submitting" aria-hidden="true">↗</span></el-button>
     </el-form>
     <p class="register-link">第一次来到这里？ <RouterLink to="/register">创建账户</RouterLink></p>
     <p class="form-footnote">不必一次懂得所有。<br />从一次小小的练习开始。</p>
    </section>
   </div>
   <section id="living-notes" class="editorial" aria-labelledby="notes-title"><div><p class="rw-kicker">01 / THE EVERYDAY DETAILS</p><h2 id="notes-title">好的居住体验，<br />藏在日常细节里。</h2><p>空间的名字，只是了解它的开始。<br />真正重要的，是它如何容纳你的生活。</p></div><ol><li><span>01</span><div><h3>光，会在什么时候进来？</h3><p>留意窗外遮挡，试着在不同时间观察采光。不只看照片，也感受通风与温度。</p></div></li><li><span>02</span><div><h3>生活，能否顺畅展开？</h3><p>从起床到出门，沿着自己的日常动线走一遍。收纳、插座、通勤，都值得认真想一想。</p></div></li><li><span>03</span><div><h3>哪些约定，需要写清楚？</h3><p>水电费用、维修责任、押金返还。把不确定的细节问清，再核对书面约定。</p></div></li></ol></section>
   <section id="risk-learning" class="learning"><p class="rw-kicker">02 / KNOW BEFORE YOU SIGN</p><h2>签字之前，<br />先练习一次看见问题。</h2><p>用模拟条款认识押金、退租、维修、费用与违约。<br />从初始诊断出发，找到需要补强的地方。</p><a class="rw-button" href="#login-form">登录，开始风险学习 <span aria-hidden="true">↗</span></a><div class="learning-path">初始诊断 <span>→</span> 能力画像 <span>→</span> 针对训练</div></section>
  </main>
  <footer class="rw-container page-footer"><strong>RentWise</strong><p>认识居住，也认识自己的选择。<br />仅用于租房风险识别教育，不提供法律意见或房源交易服务。</p><span>UI 0.1</span></footer>
 </div>
</template>

<style scoped>
.landing { padding-bottom:24px; }.skip { position:absolute; left:16px; top:-100px; z-index:10; background:white; padding:16px; }.skip:focus { top:16px; }
.nav { display:flex; justify-content:space-between; align-items:center; gap:24px; min-height:96px; border-bottom:1px solid var(--rw-line); }.wordmark { font-size:25px; font-weight:650; letter-spacing:-1px; text-decoration:none; }.wordmark span { display:block; font-size:10px; letter-spacing:.1em; font-weight:400; margin-top:6px; color:var(--rw-muted); }
nav { display:flex; gap:28px; font-size:13px; }nav a { text-decoration:none; }nav a:hover { text-decoration:underline; }
.intro { padding:52px 0 40px; }.intro h1 { font-size:clamp(32px,4vw,54px); font-weight:550; letter-spacing:-.055em; line-height:1.3; margin:20px 0; }.intro h1 span { color:#74746e; }.intro > p:last-child { font-size:14px; color:var(--rw-muted); line-height:1.8; }
.landing-panel { display:grid; grid-template-columns:3fr 2fr; border-radius:var(--rw-radius-lg); overflow:hidden; background:white; box-shadow:var(--rw-shadow); }
.login-side { padding:56px clamp(24px,4vw,64px) 32px; min-width:0; scroll-margin-top:24px; }.login-side .rw-kicker { color:var(--rw-muted); }.login-side h2 { font-size:32px; font-weight:550; letter-spacing:-.04em; margin:24px 0 12px; }.login-intro { color:var(--rw-muted); font-size:14px; line-height:1.8; margin:0 0 32px; }.login-button { width:100%; justify-content:space-between; }.login-button span { margin-left:20px; }.register-link { text-align:center; font-size:13px; line-height:1.8; margin:24px 0; color:var(--rw-muted); }.register-link a { color:var(--rw-ink); }.form-footnote { border-top:1px solid var(--rw-line); padding-top:24px; font-size:12px; line-height:1.8; color:var(--rw-muted); }.login-error { color:#a32927; font-size:13px; line-height:1.6; overflow-wrap:anywhere; }
.editorial { display:grid; grid-template-columns:1fr 1fr; gap:80px; padding:100px 24px; scroll-margin-top:24px; }.editorial h2,.learning h2 { font-size:clamp(28px,3vw,40px); font-weight:500; letter-spacing:-.04em; line-height:1.4; margin:20px 0; }.editorial p,.learning p { font-size:14px; line-height:1.9; color:var(--rw-muted); }.editorial ol { list-style:none; padding:0; margin:0; }.editorial li { display:flex; gap:24px; border-bottom:1px solid var(--rw-line); padding:20px 0; }.editorial li:first-child { padding-top:0; }.editorial li > span { padding-top:3px; font-size:11px; color:var(--rw-muted); }.editorial h3 { font-size:16px; font-weight:550; margin:0 0 10px; }.editorial li p { margin:0; }
.learning { background:#eae9e4; border-radius:var(--rw-radius-lg); text-align:center; padding:64px 24px; }.learning .rw-button { display:inline-block; text-decoration:none; margin:16px 0 32px; }.learning-path { display:flex; justify-content:center; gap:20px; font-size:12px; color:var(--rw-muted); }.page-footer { padding-top:48px; display:flex; align-items:start; justify-content:space-between; gap:24px; }.page-footer strong { font-size:20px; }.page-footer p { font-size:11px; line-height:1.8; margin:0; color:var(--rw-muted); }.page-footer > span { font-size:11px; color:var(--rw-muted); }
@media(max-width:1023px) { .landing-panel { grid-template-columns:1fr; }.login-side { padding:40px; }.editorial { gap:40px; padding:64px 0; } }
@media(max-width:767px) { .nav { min-height:80px; gap:12px; }nav { gap:16px; font-size:12px; }nav a:nth-child(2) { display:none; }.wordmark { font-size:21px; }.wordmark span { display:none; }.intro { padding:32px 0 24px; }.intro h1 { font-size:32px; }.intro .rw-kicker { font-size:9px; }.login-side { padding:28px 24px; }.editorial { grid-template-columns:1fr; gap:32px; padding:56px 8px; }.learning { padding:40px 20px; }.learning-path { gap:12px; }.page-footer { flex-wrap:wrap; padding-top:32px; }.page-footer p { width:100%; order:3; } }
</style>
