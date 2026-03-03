package com.bmad.edge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisMockDataInjectorTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void injectMockHeatMapData() {
        String cityJson = "{\"@class\":\"com.bmad.edge.dto.HeatMapDataDTO\",\"lastRefreshTime\":\"2026-03-03 23:45:00\",\"manualInterventionRate\":0.08,\"regionData\":{\"district_201\":{\"score\":95.5},\"district_301\":{\"score\":88.0}}}";
        String districtJson = "{\"@class\":\"com.bmad.edge.dto.HeatMapDataDTO\",\"lastRefreshTime\":\"2026-03-03 23:45:00\",\"manualInterventionRate\":0.02,\"regionData\":{\"school_1001\":{\"score\":92.0},\"school_1002\":{\"score\":96.5}}}";
        
        redisTemplate.opsForValue().set("dashboard:heatmap:city", cityJson);
        redisTemplate.opsForValue().set("dashboard:heatmap:district:201", districtJson);
        
        System.out.println("成功灌入 Mock 的大屏热力图缓冲数据到 Redis.");
    }
}
