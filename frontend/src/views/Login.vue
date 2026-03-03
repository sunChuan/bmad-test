<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-100">
    <div class="px-8 py-6 text-left bg-white shadow-lg rounded-lg max-w-sm w-full border border-gray-200">
      <h3 class="text-2xl font-bold text-center mb-4 text-blue-800">政务统一 SSO</h3>
      <p class="text-gray-600 text-center mb-6 text-sm">此页模拟外部政务大屏/钉钉登录中心</p>
      
      <div v-if="loading" class="flex justify-center my-4">
        <span class="text-blue-500 font-semibold animate-pulse">请求 SSO 授权回调中...</span>
      </div>
      
      <div v-else class="flex flex-col space-y-4">
        <button 
          @click="mockSsoLogin" 
          class="w-full px-4 py-2 font-bold text-white bg-blue-600 rounded hover:bg-blue-700 transition"
        >
          模拟扫码 / 免密登录授权
        </button>
        <div v-if="error" class="text-red-500 text-sm text-center">
          {{ error }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const loading = ref(false)
const error = ref('')

const mockSsoLogin = async () => {
  loading.value = true
  error.value = ''
  try {
    // Mock incoming auth code from external SSO provider
    const mockCode = 'OAUTH_CODE_' + Math.floor(Math.random() * 10000)
    
    // Call backend integration API to redeem JWT Token
    const res = await api.get('/auth/sso-callback', { params: { code: mockCode } })
    
    // Store obfuscated business JWT internally
    const token = res.data?.token
    if (token) {
      localStorage.setItem('access_token', token)
      router.push('/')
    } else {
      error.value = '授权中心拒绝了请求 (未派发 Token)'
    }
  } catch (err: any) {
    error.value = err.message || 'SSO 回调通讯失败，请检查网络或者后端的运行状况。'
  } finally {
    loading.value = false
  }
}
</script>
