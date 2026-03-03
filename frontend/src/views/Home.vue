<template>
  <div class="min-h-screen bg-slate-50 flex flex-col items-center pt-20 outline-none">
    <div class="bg-white shadow-xl rounded-xl p-8 max-w-3xl w-full text-center border-t-4 border-blue-500">
      <h1 class="text-3xl font-bold mb-4 text-slate-800">大屏内容 - 首页基座</h1>
      <p class="text-slate-600 mb-8">鉴权通道正常。您已成功通过统一身份网关并进入业务层。</p>
      
      <div class="bg-slate-100 p-4 rounded text-left mb-6 break-all border border-slate-200">
        <span class="font-semibold block mb-2 text-slate-700">🔒 已脱敏的本地业务 Token:</span>
        <code class="text-xs text-slate-500">{{ token }}</code>
      </div>

      <button 
        @click="logout" 
        class="px-6 py-2 bg-rose-500 text-white font-medium rounded hover:bg-rose-600 transition shadow-sm"
      >
        退出登录
      </button>
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
