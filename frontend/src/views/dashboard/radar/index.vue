<template>
  <div class="radar-container p-4">
    <h2 class="text-xl font-bold mb-4">全辖区聚光异常图谱</h2>
    
    <div class="relative w-full h-[600px] bg-slate-50 border rounded-lg shadow-inner overflow-hidden" style="min-height: 600px; width: 100%; min-width: 800px;">
      <!-- ECharts 渲染区 -->
      <v-chart 
        class="chart w-full h-full" 
        style="height: 600px; width: 100%;"
        :option="chartOption" 
        ref="chartRef"
        @mouseover="handleMouseOver"
        @mouseout="handleMouseOut"
        @click="handleChartClick"
      />
      
      <!-- 动态脉冲 Overlay 层，用于弥补 ECharts 内置图形动画效果不足 -->
      <div 
        v-for="(rect, index) in pulseRects" 
        :key="index"
        class="pulse-overlay"
        :style="{
          left: rect.x + 'px',
          top: rect.y + 'px',
          width: rect.width + 'px',
          height: rect.height + 'px'
        }"
      ></div>
    </div>
    
    <!-- 智能诊断与归因抽屉 -->
    <RadarDiagnosisDrawer v-model:open="drawerOpen" :schoolId="selectedSchoolId" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { use } from 'echarts/core';
import { TreemapChart } from 'echarts/charts';
import { TooltipComponent, TitleComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import VChart from 'vue-echarts';
import { useSseMessage } from '../../../composables/useSseMessage';
import RadarDiagnosisDrawer from './components/RadarDiagnosisDrawer.vue';

// 注册必须的 ECharts 组件
use([TreemapChart, TooltipComponent, TitleComponent, CanvasRenderer]);

const chartRef = ref<any>(null);
const pulseRects = ref<any[]>([]);
const { connect: connectSse } = useSseMessage();

const drawerOpen = ref(false);
const selectedSchoolId = ref<number | null>(null);

// 假装从 API 获取带有风险层级的树形模型
const mockTreeData = [
  {
    name: '中心城区',
    value: 120,
    children: [
      { name: '第一实验小学', value: 40, status: 'normal', id: 101 },
      { name: '市二中', value: 50, status: 'warning', id: 102 },
      { 
        name: '建设路联合学校', 
        value: 30, 
        status: 'danger', // 核心预警
        id: 103
      }
    ]
  },
  {
    name: '郊县A区',
    value: 80,
    children: [
      { name: '郊县一中', value: 60, status: 'danger', id: 201 },
      { name: '郊县职教', value: 20, status: 'normal', id: 202 }
    ]
  }
];

function handleChartClick(params: any) {
  if (params.data && params.data.id) {
    selectedSchoolId.value = params.data.id;
    drawerOpen.value = true;
  }
}

// ECharts Treemap 配置
const chartOption = ref({
  tooltip: {
    formatter: function (info: any) {
      const value = info.value;
      const treePathInfo = info.treePathInfo;
      const treePath = [];
      for (let i = 1; i < treePathInfo.length; i++) {
        treePath.push(treePathInfo[i].name);
      }
      return [
        '<div class="tooltip-title">' + treePath.join(' / ') + '</div>',
        '体量指标: ' + value,
        info.data.status ? '风险状态: <b class="uppercase text-xs">' + info.data.status + '</b>' : ''
      ].join('<br>');
    }
  },
  series: [
    {
      name: '辖区学校',
      type: 'treemap',
      visibleMin: 300,
      label: { show: true, formatter: '{b}' },
      itemStyle: {
        borderColor: '#fff'
      },
      levels: [
        {
          itemStyle: {
            borderWidth: 2,
            borderColor: '#333',
            gapWidth: 5
          }
        },
        {
          colorSaturation: [0.3, 0.6],
          itemStyle: { borderWidth: 1, gapWidth: 1, borderColorSaturation: 0.6 }
        }
      ],
      data: mapDataColors(mockTreeData)
    }
  ]
});

// 遍历附加不同状态颜色
function mapDataColors(data: any[]): any[] {
  return data.map(item => {
    let color = undefined;
    if (item.status === 'danger') color = '#ef4444'; // Tailwind red-500
    else if (item.status === 'warning') color = '#f59e0b'; // Tailwind amber-500
    else if (item.status === 'normal') color = '#22c55e'; // Tailwind green-500
    
    return {
      name: item.name,
      value: item.value,
      status: item.status,
      id: item.id,
      itemStyle: color ? { color } : undefined,
      children: item.children ? mapDataColors(item.children) : undefined
    };
  });
}

// 提取红色危险区块渲染 Pulse CSS
const calculatePulseOverlays = () => {
    // 由于 ECharts 不易动态向指定的方块内置持续外发光 svg，
    // 我们可以在此计算 graphic 或采用 DOM overlay 坐标提取的方式。
    // 在真实复杂环境中多调用 chartInstance.convertToPixel(...) 处理。
    // 这里为了演示概念，CSS3 Pulse 主要在后续结合 Graphic 绘制。
};

// Blur Dim 聚焦特效
const handleMouseOver = (params: any) => {
  chartRef.value?.setOption({
    series: [{
      emphasis: { focus: 'ancestor' },
      blur: {
         itemStyle: { opacity: 0.2 } // Hover 时其余节点暗暗淡出
      }
    }]
  });
};

const handleMouseOut = () => {
  // 恢复
};

onMounted(() => {
  nextTick(() => {
    calculatePulseOverlays();
  });
  // 启动 SSE 监听
  connectSse();
  
  // 提供无头浏览器 E2E 测试介入点
  if (typeof window !== 'undefined') {
    (window as any).triggerDiagnosis = (id: number) => {
      selectedSchoolId.value = id;
      drawerOpen.value = true;
    };
  }
});

</script>

<style scoped>
@import url('../../../styles/pulse.css');

.radar-container {
  height: 100%;
}
</style>
