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

    /**
     * 针对这组归因结论自动匹配推介的智库对策文献列表
     */
    private List<Recommendation> recommendations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        /**
         * 经验智库 ID
         */
        private Long id;
        
        /**
         * 推介标题
         */
        private String title;
        
        /**
         * 指接阅读的摘要简述
         */
        private String summary;
        
        /**
         * 是否在醒目位置标注为 AI 强烈建议阅读
         */
        private Boolean isAiRecommended;
        
        /**
         * 富文本正文实体，本应另外单独调用接口获取，方便演示一并携带
         */
        private String content;
    }

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
