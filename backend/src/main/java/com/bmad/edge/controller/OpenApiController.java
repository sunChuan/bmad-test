package com.bmad.edge.controller;

import com.bmad.edge.annotation.RateLimit;
import com.bmad.edge.common.Result;
import com.bmad.edge.dto.SchoolPublicRadarDTO;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

/**
 * 开放协同中心对外部机器请求下发的安全端口 (Story 4.1)
 * 该端口将被 API 专门的 Token 守卫防线保护
 */
@RestController
@RequestMapping("/open-api/v1/assessment/schools")
public class OpenApiController {

    // 伪造数据库查询并返回脱敏 DTO
    // 为了进行断崖式截流测试，这里极限配置：5 秒钟内同一 IP 仅允许点 2 下
    @RateLimit(limit = 2, timeout = 5)
    @GetMapping("/{id}")
    public Result<SchoolPublicRadarDTO> getPublicRadarProfile(@PathVariable Long id) {
        SchoolPublicRadarDTO dto = new SchoolPublicRadarDTO();
        dto.setId(id);
        dto.setName("市属实验公开校-" + id);
        
        // 模拟各象限安全离散指数 (代替原有的绝对分数)
        Random random = new Random();
        dto.setMoralityScore(60.0 + random.nextInt(40));
        dto.setIntellectScore(50.0 + random.nextInt(45));
        dto.setPhysiqueScore(70.0 + random.nextInt(30));
        dto.setAestheticScore(65.0 + random.nextInt(35));
        dto.setLaborScore(80.0 + random.nextInt(20));
        
        dto.setClusterTier("Tier " + (random.nextInt(3) + 1));
        dto.setMomentumTrend(random.nextBoolean() ? "UPTREND" : "STABLE");

        return Result.success(dto);
    }
}
