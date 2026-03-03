package com.bmad.edge.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Epic 2.1 大屏市区级宏观热力图缓存网关交互契约
 * 注意：为兼容前端图表展示和政务审计要求，包含刷新时间和人工修正率
 */
@Data
@Builder
public class HeatMapDataDTO implements Serializable {
    
    // 最近一次后台 Python 引擎刷新进 Redis 的时间 (例如：2026-03-03 14:30:00)
    private String lastRefreshTime;
    
    // 基础底层数据中被行政补录的人工干预比率
    private Double manualInterventionRate;

    // 核心区块热力图和流向指标集 (区/校 ID 映射至具体的统计分数结构，可由前端解析)
    private Map<String, Object> regionData;

}
