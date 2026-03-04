package com.bmad.edge.dto;

import lombok.Data;

@Data
public class SandboxConfigDTO {
    private Double moralityWeight; // 德育权重 
    private Double intellectWeight; // 智育权重
    private Double physiqueWeight; // 体育权重
    private Double aestheticWeight; // 美育权重
    private Double laborWeight; // 劳育权重
    
    // 其他可能的算法参变量
    private Integer clusterCount; // 聚类分层硬性数值
}
