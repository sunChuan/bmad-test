<template>
  <div class="p-8 max-w-5xl mx-auto">
    <h2 class="text-2xl font-bold mb-6 text-slate-800">对策智库 CMS：大屏引荐资料库</h2>
    
    <div class="flex flex-col md:flex-row gap-6">
      <!-- 左侧撰写 -->
      <div class="w-full md:w-2/3 bg-white p-6 rounded-lg shadow-sm border border-slate-200">
        <div class="mb-4">
          <label class="block text-slate-700 font-medium mb-2">文章大标题</label>
          <input v-model="form.title" class="w-full px-4 py-2 border rounded focus:ring" />
        </div>
        <div class="mb-4">
          <label class="block text-slate-700 font-medium mb-2">内容核心摘编 (供卡片快速浏览)</label>
          <textarea v-model="form.summary" rows="2" class="w-full px-4 py-2 border rounded focus:ring"></textarea>
        </div>
        <div class="mb-4">
          <label class="block text-slate-700 font-medium mb-2">对策匹配域 / 标签</label>
          <input v-model="form.categoryTags" placeholder="多个用逗号分隔，如：心理健康,师资对策" class="w-full px-4 py-2 border rounded focus:ring" />
        </div>
        <div class="mb-6">
          <label class="block text-slate-700 font-medium mb-2">内文编辑舱 (支持简易 Html)</label>
          <textarea v-model="form.htmlContent" rows="8" placeholder="<h1>副标题</h1><p>解决方案第一点...</p>" class="w-full px-4 py-2 font-mono text-sm border rounded bg-slate-50 focus:bg-white focus:ring"></textarea>
        </div>
        <button :disabled="loading" @click="publish" class="bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-6 rounded shadow transition">
           {{ loading ? '云端归档中...' : '审核并发表至内部图谱' }}
        </button>
      </div>
      
      <!-- 右侧文章预览列表 -->
      <div class="w-full md:w-1/3 bg-slate-50 p-6 rounded-lg border border-slate-200 overflow-y-auto max-h-[600px]">
         <h3 class="font-bold text-lg mb-4 text-slate-600 border-b pb-2">已下发的智库指南</h3>
         <button @click="fetchArticles" class="text-xs text-indigo-500 hover:underline mb-4 block">刷新文集池</button>
         
         <div v-if="articles.length === 0" class="text-slate-400 text-sm italic">当前无资料被分发...</div>
         
         <div v-for="arc in articles" :key="arc.id" class="mb-4 bg-white p-3 rounded border shadow-sm text-sm">
            <h4 class="font-bold text-slate-800 mb-1 leading-tight">{{ arc.title }}</h4>
            <div class="text-xs text-slate-400 mb-2">{{ new Date(arc.publishTime).toLocaleString() }}</div>
            <p class="text-slate-600 line-clamp-2">{{ arc.summary }}</p>
            <div class="mt-2 text-xs font-mono bg-indigo-50 text-indigo-700 inline-block px-2 py-0.5 rounded">#{{ arc.categoryTags || '未分发领域' }}</div>
         </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '../../../api';

const form = ref({
  title: '',
  summary: '',
  categoryTags: '',
  htmlContent: ''
});

const articles = ref<any[]>([]);
const loading = ref(false);

async function publish() {
  if(!form.value.title) return;
  loading.value = true;
  try {
    await api.post('/cms/article/publish', form.value);
    // 重置后获取最新
    form.value.title = ''; form.value.summary = ''; form.value.categoryTags = ''; form.value.htmlContent = '';
    await fetchArticles();
  } catch(e) {
    alert("发表受限：" + e);
  } finally {
    loading.value = false;
  }
}

async function fetchArticles() {
  try {
    const res = await api.get('/cms/article/list');
    articles.value = res.data || [];
  } catch(e) {
    console.error("加载文集失败", e);
  }
}

onMounted(() => fetchArticles());
</script>
