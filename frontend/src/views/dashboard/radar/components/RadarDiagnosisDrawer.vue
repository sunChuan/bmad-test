<template>
  <Drawer
    :open="open"
    @update:open="$emit('update:open', $event)"
    title="智能归因诊断"
    placement="right"
    width="500"
  >
    <!-- 使用 Skeleton 展示 1.5s 的加载过渡骨架 -->
    <Skeleton active :loading="loading" :paragraph="{ rows: 6 }">
      <div v-if="diagnosisData">
        <!-- 核心结论与置信度 -->
        <div class="mb-6 p-4 bg-red-50 rounded border border-red-100">
          <div class="text-red-600 font-bold text-lg mb-2 flex items-center">
            <span class="mr-2">🚨</span> {{ diagnosisData.conclusion }}
          </div>
          <div class="text-gray-600 text-sm">
            AI 归因置信度: <span class="font-bold text-gray-800">{{ diagnosisData.confidence }}%</span>
          </div>
        </div>

        <!-- 手风琴：折叠的致病因子与走势 -->
        <Collapse v-model:activeKey="activeKey" class="bg-white">
          <CollapsePanel 
            v-for="(factor, idx) in diagnosisData.factors" 
            :key="idx.toString()" 
            :header="`${factor.name} (影响系数: ${factor.score})`"
          >
            <div class="h-48 w-full">
              <v-chart class="chart" :option="getChartOption(factor)" autoresize />
            </div>
          </CollapsePanel>
        </Collapse>
      </div>
      <div v-else-if="!loading" class="text-gray-400 text-center mt-10">
        无有效的归因数据
      </div>
    </Skeleton>
  </Drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { Drawer, Skeleton, Collapse, CollapsePanel } from 'ant-design-vue';
import api from '../../../../api/index';
import { use } from 'echarts/core';
import { LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import VChart from 'vue-echarts';

// 注册子折线图表组件
use([LineChart, GridComponent, TooltipComponent, CanvasRenderer]);

const props = defineProps<{
  open: boolean;
  schoolId: number | null;
}>();

const emit = defineEmits(['update:open']);

const loading = ref(false);
const diagnosisData = ref<any>(null);
const activeKey = ref(['0']);

watch(() => props.open, async (newVal) => {
  if (newVal && props.schoolId) {
    loading.value = true;
    diagnosisData.value = null;
    try {
      const res = await api.get(`/dashboard/radar/diagnosis/${props.schoolId}`);
      diagnosisData.value = res.data;
    } catch (e) {
      console.error('Fetch diagnosis failed:', e);
    } finally {
      loading.value = false;
    }
  }
});

function getChartOption(factor: any) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '10%', right: '5%', bottom: '15%', top: '10%' },
    xAxis: {
      type: 'category',
      data: factor.xAxis || []
    },
    yAxis: {
      type: 'value',
      scale: true
    },
    series: [
      {
        data: factor.history || [],
        type: 'line',
        smooth: true,
        itemStyle: { color: '#ef4444' }, // Red-500
        lineStyle: { color: '#ef4444', width: 2 },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [{ offset: 0, color: 'rgba(239, 68, 68, 0.3)' }, { offset: 1, color: 'rgba(239, 68, 68, 0.0)' }]
          }
        }
      }
    ]
  };
}
</script>

<style scoped>
.chart {
  width: 100%;
  height: 100%;
}
/* 强行覆盖一些 Ant Design 样式让其更加扁平 */
:deep(.ant-collapse) {
  border-radius: 8px;
  overflow: hidden;
}
</style>
