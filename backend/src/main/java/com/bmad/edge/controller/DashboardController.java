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
                .build();
                
        return Result.success(dto);
    }
}
