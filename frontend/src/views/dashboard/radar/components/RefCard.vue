<template>
  <div class="ref-card-wrapper transition-all hover:scale-[1.01] cursor-pointer" @click="openDetail">
    <div class="bg-gradient-to-br from-indigo-50 to-white border border-indigo-100 rounded-xl p-4 shadow-sm relative overflow-hidden">
      <!-- AI 强推徽章 -->
      <div v-if="data.isAiRecommended" class="absolute top-0 right-0 bg-yellow-400 text-yellow-900 text-xs font-bold px-3 py-1 rounded-bl-lg shadow-sm flex items-center">
        <span class="mr-1">✨</span> AI 强推
      </div>
      
      <div class="flex flex-col h-full">
        <h3 class="text-indigo-900 font-bold text-base mb-2 pr-12 line-clamp-2">{{ data.title }}</h3>
        <p class="text-gray-600 text-sm line-clamp-3 mb-3 flex-grow">{{ data.summary }}</p>
        <div class="flex items-center text-indigo-500 text-xs font-medium mt-auto">
          <span>参阅详案</span>
          <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path></svg>
        </div>
      </div>
    </div>

    <!-- 内嵌的富文本详情阅读 Drawer -->
    <Drawer
      v-model:open="isDetailOpen"
      :title="data.title"
      placement="right"
      width="600"
      @click.stop
    >
      <div class="article-content prose prose-indigo max-w-none">
        <div v-html="data.content"></div>
      </div>
    </Drawer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Drawer } from 'ant-design-vue';

const props = defineProps<{
  data: {
    id: number;
    title: string;
    summary: string;
    isAiRecommended: boolean;
    content: string; // 包含 HTML
  }
}>();

const isDetailOpen = ref(false);

function openDetail() {
  isDetailOpen.value = true;
}
</script>

<style scoped>
/* 可以在此定义一些针对文章内部标签的复写样式 */
.article-content :deep(h2) {
  @apply text-xl font-bold text-gray-800 mb-4 pb-2 border-b border-gray-200;
}
.article-content :deep(p) {
  @apply text-gray-600 mb-4 leading-relaxed;
}
.article-content :deep(ul), .article-content :deep(ol) {
  @apply pl-5 mb-4 text-gray-600 space-y-2;
}
.article-content :deep(li) {
  @apply list-decimal;
}
.article-content :deep(em) {
  @apply text-gray-400 text-sm italic;
}
</style>
