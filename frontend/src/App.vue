<template>
  <div v-if="fatalError" class="p-10 bg-red-100 min-h-screen text-red-900">
    <h1 class="text-3xl font-bold mb-4">应用遭遇崩溃！并不是空白</h1>
    <pre class="bg-red-50 p-4 border border-red-200 rounded overflow-auto">{{ fatalError }}</pre>
  </div>
  <router-view v-else></router-view>
</template>

<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue';

const fatalError = ref<string | null>(null);

onErrorCaptured((err, instance, info) => {
  fatalError.value = `${err.toString()}\n\nInfo: ${info}\n\nStack:\n${err.stack}`;
  return false; // 阻止向上传递
});

// 增加 Window 级别的全量捕捉
if (typeof window !== 'undefined') {
  window.addEventListener('error', (event) => {
    fatalError.value = `[Window Error] ${event.message}\n${event.error?.stack}`;
  });
  window.addEventListener('unhandledrejection', (event) => {
    fatalError.value = `[Unhandled Promise] ${event.reason}`;
  });
}
</script>
