package com.bmad.edge.controller;

import com.bmad.edge.common.Result;
import com.bmad.edge.dto.HeatMapDataDTO;
import com.bmad.edge.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

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
}
