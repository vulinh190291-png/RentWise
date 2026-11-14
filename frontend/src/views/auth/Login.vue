
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
  <header class="rw-container nav"><a class="wordmark" href="#">RentWise<span>居住有知，选择有底。</span></a><nav aria-label="主导航"><a href="#living-notes">居住知识</a><a href="#risk-learning">风险学习</a><a class="nav-login" href="#login-form">登录</a><RouterLink to="/register">创建账户 <span aria-hidden="true">↗</span></RouterLink></nav></header>
  <main class="rw-container">
   <div class="intro"><div><p class="rw-kicker">RENTWISE / 居住与选择</p><h1>读懂租约，<span>也读懂生活。</span></h1></div><p>从探索居住空间，到练习识别租约风险。<br />RentWise 帮你把喜欢的生活，变成更有把握的选择。</p></div>
   <div class="landing-panel">
    <HousingShowcase />
    <section id="login-form" class="login-side" tabindex="-1" aria-labelledby="login-title">
     <p class="rw-kicker">YOUR LEARNING SPACE</p><h2 id="login-title">欢迎回来。</h2><p class="login-intro">从一次练习开始，看清签约前的关键问题。</p>
     <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="submitting" @submit.prevent="handleLogin">
      <el-form-item label="用户名" prop="username"><el-input id="username" v-model="form.username" autocomplete="username" placeholder="输入用户名" size="large" :prefix-icon="User" /></el-form-item>
      <el-form-item label="密码" prop="password"><el-input id="password" v-model="form.password" type="password" autocomplete="current-password" placeholder="输入密码" size="large" show-password :prefix-icon="Lock" /></el-form-item>
      <p v-if="loginError" class="login-error" role="alert">{{ loginError }}</p>
      <el-button type="primary" native-type="submit" class="login-button" :loading="submitting" :disabled="submitting">{{ submitting ? '正在登录' : '登录，继续学习' }}<span v-if="!submitting" aria-hidden="true">↗</span></el-button>
     </el-form>
     <p class="register-link">第一次来到这里？ <RouterLink to="/register">创建账户</RouterLink></p>
    </section>
   </div>
   <section id="living-notes" class="editorial" aria-labelledby="notes-title"><div><p class="rw-kicker">01 / THE EVERYDAY DETAILS</p><h2 id="notes-title">喜欢一处空间，<br />也要看清日常。</h2><p>居住探索帮你找到值得观察的细节。<br />风险学习帮你把这些问题带进租约。</p></div><ol><li><span>01</span><div><h3>光，会在什么时候进来？</h3><p>留意窗外遮挡，试着在不同时间观察采光。不只看照片，也感受通风与温度。</p></div></li><li><span>02</span><div><h3>生活，能否顺畅展开？</h3><p>从起床到出门，沿着自己的日常动线走一遍。收纳、插座、通勤，都值得认真想一想。</p></div></li><li><span>03</span><div><h3>哪些约定，需要写清楚？</h3><p>水电费用、维修责任、押金返还。把不确定的细节问清，再核对书面约定。</p></div></li></ol></section>
   <section id="risk-learning" class="learning"><p class="rw-kicker">02 / KNOW BEFORE YOU SIGN</p><h2>签字之前，<br />先练习一次看见问题。</h2><p>用模拟条款认识押金、退租、维修、费用与违约。<br />从初始诊断出发，找到需要补强的地方。</p><a class="rw-button" href="#login-form">登录，开始风险学习 <span aria-hidden="true">↗</span></a><div class="learning-path">初始诊断 <span>→</span> 能力画像 <span>→</span> 针对训练</div></section>
  </main>
  <footer class="rw-container page-footer"><strong>RentWise</strong><p>认识居住，也认识自己的选择。<br />仅用于租房风险识别教育，不提供法律意见或房源交易服务。</p><span>UI 0.1.1</span></footer>
 </div>
</template>

<style scoped>
.landing { padding-bottom:24px; background:#f7f6f2; }
.skip { position:absolute; left:16px; top:-100px; z-index:10; background:white; padding:16px; }
.skip:focus { top:16px; }
.nav { display:flex; justify-content:space-between; align-items:center; gap:24px; min-height:80px; border-bottom:1px solid var(--rw-line); }
.wordmark { font-size:25px; font-weight:650; letter-spacing:-1px; text-decoration:none; }
.wordmark span { display:block; font-size:10px; letter-spacing:.1em; font-weight:400; margin-top:4px; color:var(--rw-muted); }
nav { display:flex; align-items:center; gap:26px; font-size:13px; }
nav a { text-decoration:none; } nav a:hover { text-decoration:underline; }
.nav-login { border-bottom:1px solid currentColor; padding-block:8px; }
.intro { display:flex; justify-content:space-between; align-items:end; gap:32px; padding:30px 0 28px; }
.intro .rw-kicker { margin:0 0 12px; }
.intro h1 { font-size:clamp(30px,3vw,42px); font-weight:550; letter-spacing:-.05em; line-height:1.3; margin:0; }
.intro h1 span { color:#70706a; }
.intro > p { max-width:390px; font-size:13px; color:var(--rw-muted); line-height:1.9; margin:0; text-wrap:pretty; }
.landing-panel { display:grid; grid-template-columns:3fr 2fr; border:1px solid #e9e6de; border-radius:var(--rw-radius-lg); overflow:hidden; background:white; box-shadow:0 12px 40px #27231b06; }
.login-side { padding:32px clamp(28px,3.8vw,54px) 24px; min-width:0; scroll-margin-top:16px; }
.login-side > .rw-kicker { color:var(--rw-muted); margin:0 0 12px; line-height:16px; }
.login-side h2 { font-size:30px; font-weight:550; letter-spacing:-.04em; line-height:1.3; margin:0 0 10px; }
.login-intro { color:var(--rw-muted); font-size:13px; line-height:1.8; margin:0 0 24px; }
.login-side :deep(.el-form-item) { margin-bottom:22px; }
.login-button { width:100%; justify-content:space-between; }
.login-button span { margin-left:20px; }
.register-link { text-align:center; font-size:13px; line-height:1.8; margin:20px 0 0; color:var(--rw-muted); }
.register-link a { color:var(--rw-ink); }
.login-error { color:#a32927; font-size:13px; line-height:1.6; overflow-wrap:anywhere; }
.editorial { display:grid; grid-template-columns:1fr 1.15fr; gap:80px; padding:72px 24px; scroll-margin-top:24px; }
.editorial h2,.learning h2 { font-size:clamp(26px,2.5vw,36px); font-weight:500; letter-spacing:-.04em; line-height:1.4; margin:16px 0; }
.editorial p,.learning p { font-size:14px; line-height:1.9; color:var(--rw-muted); }
.editorial ol { list-style:none; padding:0; margin:0; }
.editorial li { display:flex; gap:24px; border-bottom:1px solid var(--rw-line); padding:20px 0; }
.editorial li:first-child { padding-top:0; }
.editorial li > span { padding-top:3px; font-size:11px; color:var(--rw-muted); }
.editorial h3 { font-size:16px; font-weight:550; margin:0 0 8px; }
.editorial li p { margin:0; }
.learning { background:#eeede7; border-radius:var(--rw-radius-lg); text-align:center; padding:48px 24px; }
.learning .rw-button { display:inline-block; text-decoration:none; margin:12px 0 24px; }
.learning-path { display:flex; justify-content:center; gap:20px; font-size:12px; color:var(--rw-muted); }
.page-footer { padding-top:36px; display:flex; align-items:start; justify-content:space-between; gap:24px; }
.page-footer strong { font-size:20px; }.page-footer p { margin:0; font-size:11px; line-height:1.8; color:var(--rw-muted); }.page-footer > span { font-size:11px; color:var(--rw-muted); }
@media (min-width:1024px) and (max-height:800px) {
 .nav { min-height:68px; }.intro { padding-block:22px; }.intro h1 { font-size:32px; }.intro > p { max-width:340px; font-size:12px; }.login-side { padding-top:28px; padding-bottom:24px; }
}
@media(max-width:1023px) {
 .intro { align-items:start; flex-direction:column; gap:16px; }.intro > p { max-width:540px; }
 .landing-panel { grid-template-columns:1fr; }.login-side { padding:32px 40px; }.editorial { gap:40px; padding:56px 8px; }
}
@media(max-width:767px) {
 .nav { min-height:68px; gap:12px; }nav { gap:16px; font-size:12px; }nav a:nth-child(1),nav a:nth-child(2) { display:none; }
 .wordmark { font-size:22px; }.wordmark span { display:none; }.intro { padding:24px 0; gap:12px; }.intro h1 { font-size:30px; }.intro h1 span { display:block; }.intro .rw-kicker { font-size:9px; margin-bottom:10px; }.intro > p { font-size:12px; }.intro > p br { display:none; }
 .login-side { padding:28px 24px; }.login-side h2 { font-size:28px; }.login-intro { margin-bottom:20px; }
 .editorial { grid-template-columns:1fr; gap:28px; padding:48px 8px; }.learning { padding:36px 20px; }.learning-path { gap:12px; }.page-footer { flex-wrap:wrap; padding-top:28px; }.page-footer p { width:100%; order:3; }
}
</style>
