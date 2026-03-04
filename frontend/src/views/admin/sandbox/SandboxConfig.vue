<template>
  <div class="p-8 max-w-4xl mx-auto">
    <h2 class="text-2xl font-bold mb-6 text-slate-800">全域教研沙箱：大基线模拟试跑台</h2>
    
    <!-- 调参舱 -->
    <div v-if="!taskId" class="bg-white p-6 rounded-lg shadow-sm border border-slate-200">
      <p class="text-slate-600 mb-6 border-b pb-4">请调节各项核心素养对区域总评划档模型的权重干预率。总和不限，算法引擎将自动按比例缩放归一化。</p>

      <div class="space-y-6 mb-8">
        <div v-for="(slider, key) in sliders" :key="key" class="flex items-center">
            <span class="w-32 font-bold text-slate-700">{{ slider.label }}</span>
            <input type="range" min="0" max="100" v-model.number="config[key]" class="flex-grow ml-4 mr-4 accent-indigo-600">
            <span class="w-12 text-right font-mono text-indigo-600 font-bold">{{ config[key] }}%</span>
        </div>
        
        <div class="flex items-center pt-4 border-t">
            <span class="w-32 font-bold text-slate-700">聚类落档数限制</span>
            <input type="number" min="1" max="10" v-model.number="config.clusterCount" class="w-24 px-3 py-1 ml-4 border rounded focus:ring">
        </div>
      </div>
      
      <button :disabled="starting" @click="startSimulation" class="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-3 px-4 rounded transition text-lg shadow">
         {{ starting ? '正在推送装配指令至远程队列...' : '装载参数并启动全区 150 校重计算推演' }}
      </button>
    </div>

    <!-- 进度监听舱 -->
    <div v-else class="bg-slate-50 p-8 rounded-lg border-2 border-indigo-100 flex flex-col items-center justify-center min-h-[400px]">
       <div v-if="progress < 100" class="w-full text-center">
           <div class="animate-spin inline-block w-12 h-12 border-4 border-indigo-500 border-t-transparent rounded-full mb-6"></div>
           <h3 class="text-xl font-bold text-indigo-800 mb-2">底层 Python 集群正在重算 150 所学校的指标距...</h3>
           <p class="text-slate-500 mb-8 font-mono">分配追踪码: {{ taskId }}</p>
           
           <div class="w-full bg-gray-200 rounded-full h-4 mb-2 overflow-hidden shadow-inner">
             <div class="bg-indigo-600 h-4 rounded-full transition-all duration-500 ease-out flex items-center justify-end pr-2" :style="{ width: progress + '%' }">
               <span class="text-[10px] text-white font-bold opacity-80" v-if="progress > 5">{{ progress }}%</span>
             </div>
           </div>
           <p class="text-xs text-slate-400 text-right">长轮询心跳保持中 (Interval: 1s)</p>
       </div>
       
       <!-- 测算完成的验收舱 -->
       <div v-else class="text-center w-full">
           <div class="w-16 h-16 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto mb-4">
             <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path></svg>
           </div>
           <h3 class="text-2xl font-bold text-green-700 mb-2">沙盘模拟计算彻底跑通！</h3>
           <p class="text-slate-600 mb-8">所有学校的最新梯座已在隔离区存储完毕。您可以查阅结果或直接取代主干。</p>
           
           <button :disabled="publishing" @click="publishBaseline" class="w-full sm:w-auto px-8 py-3 bg-rose-500 hover:bg-rose-600 text-white font-bold rounded shadow-lg transition">
             {{ publishing ? '缓存网关重置中...' : '审核无误，一键将其覆写至正式大屏架构' }}
           </button>
           
           <div class="mt-4 text-center">
             <button @click="resetTask" class="text-sm text-slate-400 hover:text-slate-600 underline">放弃并重新调参</button>
           </div>
       </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount } from 'vue';
import api from '../../../api';

const config = ref<any>({
  moralityWeight: 20,
  intellectWeight: 40,
  physiqueWeight: 15,
  aestheticWeight: 15,
  laborWeight: 10,
  clusterCount: 4
});

const sliders = {
  moralityWeight: { label: '德育表现加压率' },
  intellectWeight: { label: '智力成绩含金量' },
  physiqueWeight: { label: '体能测评权重' },
  aestheticWeight: { label: '美育赛事获奖' },
  laborWeight: { label: '综合劳技素养' },
};

const starting = ref(false);
const taskId = ref<string | null>(null);
const progress = ref(0);
const publishing = ref(false);
let pollTimer: any = null;

async function startSimulation() {
  starting.value = true;
  try {
    const res = await api.post('/sandbox/config', config.value);
    taskId.value = res.data.taskId;
    progress.value = 0;
    startPolling();
  } catch (e: any) {
    alert("沙箱指令异常: " + e.message);
  } finally {
    starting.value = false;
  }
}

function startPolling() {
  pollTimer = setInterval(async () => {
    if (!taskId.value) return;
    try {
      const res = await api.get(`/sandbox/task/${taskId.value}/progress`);
      progress.value = res.data;
      if (progress.value >= 100) {
        clearInterval(pollTimer);
      }
    } catch (e) {
      clearInterval(pollTimer);
      alert("长轮询中断！跑批可能在远程服务被终结！");
      resetTask();
    }
  }, 1000);
}

async function publishBaseline() {
  publishing.value = true;
  try {
    const res = await api.post('/sandbox/publish-baseline');
    alert("【发布成功】\n\n新模型生效：" + res.message);
    resetTask();
  } catch (e: any) {
    alert("覆写生产树失败: " + e.message);
  } finally {
    publishing.value = false;
  }
}

function resetTask() {
  if (pollTimer) clearInterval(pollTimer);
  taskId.value = null;
  progress.value = 0;
}

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer);
});
</script>
