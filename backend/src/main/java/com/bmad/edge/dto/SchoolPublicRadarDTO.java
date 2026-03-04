package com.bmad.edge.dto;

import lombok.Data;

/**
 * Epic 4 对外开放的画像脱敏查询对象
 * 严禁暴露校长名、全区分数明细及未经加工的底层参数 (FR17, NFR2)
 */
@Data
public class SchoolPublicRadarDTO {
    private Long id;
    private String name; // 学校名称，但无校长及联络人
    
    // 只保留五育等高层级的脱敏或离散聚类数据
    private Double moralityScore;
    private Double intellectScore;
    private Double physiqueScore;
    private Double aestheticScore;
    private Double laborScore;
    
    // 算法给出的同层级梯队代号 (例如: Tier 1)
    private String clusterTier;
    
    // 综合增长势能 (正面 vs 负面，不给具体小数点分数)
    private String momentumTrend;
}
