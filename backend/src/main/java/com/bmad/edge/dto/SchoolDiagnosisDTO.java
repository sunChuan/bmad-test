package com.bmad.edge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDiagnosisDTO {
    
    /**
     * 最大归因结论文本，如 "最大剥离因子为：师资流失率激增"
     */
    private String conclusion;
    
    /**
     * 算法置信度，如 92.5
     */
    private Double confidence;
    
    /**
     * 导致排名下降或波动的具体指标明细因子列表
     */
    private List<Factor> factors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Factor {
        /**
         * 因子名称，如 "师生流失率"
         */
        private String name;
        
        /**
         * 当前得分或负面影响度占比
         */
        private Double score;
        
        /**
         * 历史周期走势（用于在手风琴内部画折线图），时间倒序或正序的数值数组
         */
        private List<Double> history;
        
        /**
         * 历史轴标签 (可选，通常前后端约定即可)
         */
        private List<String> xAxis;
    }
}
