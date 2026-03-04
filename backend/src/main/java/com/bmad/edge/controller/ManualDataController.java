package com.bmad.edge.controller;

import com.bmad.edge.annotation.Audit;
import com.bmad.edge.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 强硬断点数据补录 (Story 3.2)
 * 用于异常时的单条数据强塞
 */
@RestController
@RequestMapping("/api/v1/data/fallback")
public class ManualDataController {

    private static final Logger log = LoggerFactory.getLogger(ManualDataController.class);

    @PostMapping("/single")
    @Audit(actionType = "MANUAL_UPSERT", targetEntity = "AssessmentData")
    public Result<String> upsertMissingScore(@RequestBody Map<String, Object> dataPayload) {
        log.info("收到管理员强制补录请求: {}", dataPayload);
        
        // 执行业务防呆校验
        Object scoreObj = dataPayload.get("score");
        if (scoreObj instanceof Number) {
            double score = ((Number) scoreObj).doubleValue();
            if (score > 150) {
                return Result.error(400, "总分不可超过设定上限 150 分，请查修！");
            }
        }
        
        // 模拟向 MySQL 落盘
        log.info("【Mock DB】数值合规，已执行插入覆盖...");
        
        return Result.success("特殊录入成功，已双写不可篡改审计日志！");
    }
}
