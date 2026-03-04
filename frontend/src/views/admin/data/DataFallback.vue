<template>
  <div class="p-8 max-w-4xl mx-auto">
    <h2 class="text-2xl font-bold mb-6 text-slate-800">数据库防线：底槽异常补充断点</h2>
    
    <div class="bg-white p-6 rounded-lg shadow-sm border border-slate-200">
      <div class="mb-4">
        <label class="block text-slate-700 font-medium mb-2">缺口学校 ID</label>
        <input v-model="form.schoolId" type="number" class="w-full px-4 py-2 border rounded focus:ring focus:ring-blue-200" placeholder="e.g. 1001" />
      </div>
      
      <div class="mb-6">
        <label class="block text-slate-700 font-medium mb-2">手动纠偏的基准分 (上限 150)</label>
        <input v-model.number="form.score" type="number" class="w-full px-4 py-2 border rounded focus:ring focus:ring-blue-200" placeholder="不能超过 150 分，否则会被阻隔" />
      </div>
      
      <div v-if="alertMsg" :class="['p-4 mb-4 rounded text-sm', isError ? 'bg-red-50 text-red-600 border border-red-200' : 'bg-green-50 text-green-700 border border-green-200']">
        {{ alertMsg }}
      </div>

      <button :disabled="loading" @click="submit" class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded transition">
        <span v-if="loading">安全通信中与审计双写中...</span>
        <span v-else>强制保存补录日志</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import api from '../../../api';

const form = ref({ schoolId: '', score: null as number | null });
const alertMsg = ref('');
const isError = ref(false);
const loading = ref(false);

async function submit() {
  alertMsg.value = '';
  if (!form.value.schoolId || form.value.score === null) {
    isError.value = true;
    alertMsg.value = '各项均不得为空！';
    return;
  }
  if (form.value.score > 150 || form.value.score < 0) {
    isError.value = true;
    alertMsg.value = '[前端防伪拦截]：数据中心最高分不许超过 150 分，请查实后重新输批。';
    return;
  }

  loading.value = true;
  try {
    const res = await api.post('/data/fallback/single', form.value);
    isError.value = false;
    alertMsg.value = `执行成功！[ ${res.message || '双写不可逆转'} ]`;
    form.value.schoolId = '';
    form.value.score = null;
  } catch (e: any) {
    isError.value = true;
    alertMsg.value = `操作被后端核验器阻断：${e.message || 'Error'}`;
  } finally {
    loading.value = false;
  }
}
</script>
