<template>
  <div class="min-h-screen bg-slate-50 flex flex-col items-center pt-10 outline-none pb-20">
    <!-- Header -->
    <div class="bg-indigo-600 w-full text-white text-center py-6 shadow-md mb-8">
      <h1 class="text-3xl font-bold tracking-widest">教育局数据中枢矩阵</h1>
      <p class="mt-2 text-indigo-100 font-medium">微服务集群控制台 ＆ Epic 集成验收平台</p>
    </div>

    <!-- Entrance Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-8 w-full max-w-5xl px-4">
      
      <!-- Epic 2: Radar -->
      <div @click="$router.push('/dashboard/radar')" class="bg-white hover:-translate-y-2 transition-transform duration-300 rounded-xl shadow border border-slate-200 p-8 cursor-pointer flex flex-col items-center text-center group">
        <div class="w-16 h-16 bg-blue-100 text-blue-600 rounded-2xl flex items-center justify-center mb-6 group-hover:bg-blue-600 group-hover:text-white transition-colors">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
        </div>
        <h2 class="text-2xl font-bold text-slate-800 mb-2">雷达指挥舱 (Epic 2)</h2>
        <p class="text-slate-500">查看带有异常标红和 AI 归因诊断的全辖区聚光大盘</p>
      </div>

      <!-- Epic 3: Sandbox -->
      <div @click="$router.push('/admin/sandbox')" class="bg-white hover:-translate-y-2 transition-transform duration-300 rounded-xl shadow border border-slate-200 p-8 cursor-pointer flex flex-col items-center text-center group">
        <div class="w-16 h-16 bg-purple-100 text-purple-600 rounded-2xl flex items-center justify-center mb-6 group-hover:bg-purple-600 group-hover:text-white transition-colors">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" /></svg>
        </div>
        <h2 class="text-2xl font-bold text-slate-800 mb-2">底层算法推演沙箱</h2>
        <p class="text-slate-500">调整各育比重、发起 Python 并发计算池并生成大基线</p>
      </div>

      <!-- Epic 3: Data Fallback -->
      <div @click="$router.push('/admin/data/fallback')" class="bg-white hover:-translate-y-2 transition-transform duration-300 rounded-xl shadow border border-slate-200 p-8 cursor-pointer flex flex-col items-center text-center group">
        <div class="w-16 h-16 bg-orange-100 text-orange-600 rounded-2xl flex items-center justify-center mb-6 group-hover:bg-orange-600 group-hover:text-white transition-colors">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4" /></svg>
        </div>
        <h2 class="text-2xl font-bold text-slate-800 mb-2">断点强制补录面板</h2>
        <p class="text-slate-500">模拟解决抽取挂断时直接写入核心库并附带安全防篡体</p>
      </div>

      <!-- Epic 3: Article CMS -->
      <div @click="$router.push('/admin/cms/article')" class="bg-white hover:-translate-y-2 transition-transform duration-300 rounded-xl shadow border border-slate-200 p-8 cursor-pointer flex flex-col items-center text-center group">
        <div class="w-16 h-16 bg-emerald-100 text-emerald-600 rounded-2xl flex items-center justify-center mb-6 group-hover:bg-emerald-600 group-hover:text-white transition-colors">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" /></svg>
        </div>
        <h2 class="text-2xl font-bold text-slate-800 mb-2">对策智囊管理中枢</h2>
        <p class="text-slate-500">以带标签的富文本写入系统，供前端动态抽屉提取并指控</p>
      </div>
    </div>
    
    <div class="mt-12 text-center text-slate-400">
      <div class="mb-4">
        🔒 Security Token: <code class="text-xs bg-slate-200 px-2 py-1 rounded">{{ token.substring(0, 40) }}...</code>
      </div>
      <button @click="logout" class="px-6 py-2 border border-slate-300 text-slate-500 rounded hover:bg-slate-100 transition shadow-sm">退出登录 / 清除令牌</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const token = ref(localStorage.getItem('access_token') || '')

const logout = () => {
  localStorage.removeItem('access_token')
  router.push('/login')
}
</script>
