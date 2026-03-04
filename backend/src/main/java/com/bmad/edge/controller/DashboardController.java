package com.bmad.edge.controller;

import com.bmad.edge.common.Result;
import com.bmad.edge.dto.HeatMapDataDTO;
import com.bmad.edge.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.bmad.edge.config.TenantContextHolder;
import com.bmad.edge.service.SseConnectionService;
import com.bmad.edge.dto.SchoolDiagnosisDTO;

import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final SseConnectionService sseConnectionService;

    /**
     * 获取市、区级宏观热力图。
     * 权限拦截：只允许市级管理员、区级评估局长及拥有 dashboard:macro 权限的角色访问。
     */
    @GetMapping("/macro/heat-map")
    @PreAuthorize("hasAnyRole('CITY_ADMIN', 'DISTRICT_DIRECTOR')")
    public Result<HeatMapDataDTO> getMacroHeatMap() {
        HeatMapDataDTO heatMapData = dashboardService.getMacroHeatMap();
        return Result.success(heatMapData);
    }

    /**
     * SSE 客户端长连接注入 / 订阅通道
     * @return 挂起后的 SseEmitter
     */
    @GetMapping(value = "/sse/subscribe", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeSse() {
        // 利用系统已经解析完毕存入的上下文，提取会话级别（如某区级）身份
        String rolePrefix = TenantContextHolder.getCurrentRole();
        Long tenantId = TenantContextHolder.getCurrentTenantId();
        
        // 构造 Client ID, 如: city_admin_0, district_director_201
        String clientId = rolePrefix + "_" + (tenantId != null ? tenantId : "0");
        return sseConnectionService.createConnection(clientId);
    }
    
    /**
     * 【内部模拟预警使用】发送全广播或者针对某级别的定向推送
     * @param filterPrefix 接收对象筛选器前缀，如 "district_" 或为空代表全部
     */
    @PostMapping("/sse/trigger-alert")
    public Result<String> triggerAlertMock(@RequestParam(defaultValue = "") String filterPrefix,
                                           @RequestParam String message) {
        sseConnectionService.broadcastToPrefix(filterPrefix, message);
        return Result.success("预警消息下发成功: " + message);
    }

    /**
     * 智能诊断抽屉数据检索（Mock）
     * 提供一个虚拟 1.5s 延迟以在前端展示 Skeleton
     * @param schoolId 学校 ID
     */
    @GetMapping("/radar/diagnosis/{schoolId}")
    public Result<SchoolDiagnosisDTO> getSchoolDiagnosis(@PathVariable Long schoolId) {
        try {
            // 模拟后台 Python 通信、决策树溯源的耗时
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 构造虚假的回溯结论
        SchoolDiagnosisDTO dto = SchoolDiagnosisDTO.builder()
                .conclusion("最大剥离因子为：师资流失率与心理健康抽测骤降")
                .confidence(92.5)
                .factors(List.of(
                        SchoolDiagnosisDTO.Factor.builder()
                                .name("近一年骨干教师流失")
                                .score(-12.4)
                                .xAxis(Arrays.asList("24-Q1", "24-Q2", "24-Q3", "24-Q4", "25-Q1", "26-Q1"))
                                .history(Arrays.asList(1.2, 2.5, 3.1, 4.0, 9.8, 12.4)) // 明显上升的流失率
                                .build(),
                        SchoolDiagnosisDTO.Factor.builder()
                                .name("学生心理压线筛查比例")
                                .score(-8.1)
                                .xAxis(Arrays.asList("24-Q1", "24-Q2", "24-Q3", "24-Q4", "25-Q1", "26-Q1"))
                                .history(Arrays.asList(5.5, 5.2, 5.0, 6.1, 7.8, 8.1)) 
                                .build()
                ))
                .recommendations(List.of(
                        SchoolDiagnosisDTO.Recommendation.builder()
                                .id(101L)
                                .title("市教育局下达本季度骨干教师安居补贴执行方案的红头文件")
                                .summary("针对近期辖区内因待遇及购房压力产生的教师流失率升高，发布了阶段性强力留才薪酬补充政策指引。")
                                .isAiRecommended(true)
                                .content("<h2>骨干教师安居补贴执行方案</h2><p>为切实削减近年教育管理骨干外流的现象，建立稳定的名师梯队，市委经讨论决定：</p><ul><li>对满5年教龄的市级学科带头人一次性给予安居补贴 10 万元；</li><li>配合职称评定给予特殊岗位积分偏斜。</li></ul><p><em>本文档仅供内部参考部署</em></p>")
                                .build(),
                        SchoolDiagnosisDTO.Recommendation.builder()
                                .id(102L)
                                .title("春季学期校园学生心理危机预防及干预措施汇总")
                                .summary("全辖境推行的分层心理预防辅导实战方案集锦。")
                                .isAiRecommended(false)
                                .content("<h2>校园学生心理危机预防干预措施</h2><p>应对日益突显的身心异常报警，特下发以下强制防范预警指导：</p><ol><li>各校需在本周内全覆盖实行无死角的问卷筛查。</li><li>建立高危名单班主任直系包保制度。</li></ol>")
                                .build()
                ))
                .build();
                
        return Result.success(dto);
    }
}
