package com.bmad.edge.controller;

import com.bmad.edge.dto.HeatMapDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mock")
public class MockController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/inject-redis")
    public String injectRedis() {
        // Mock City Data
        HeatMapDataDTO cityData = HeatMapDataDTO.builder()
                .lastRefreshTime("2026-03-03 23:45:00")
                .manualInterventionRate(0.08)
                .regionData(Map.of("district_201", Map.of("score", 95.5), "district_301", Map.of("score", 88.0)))
                .build();
                
        // Mock District 201 Data
        HeatMapDataDTO districtData = HeatMapDataDTO.builder()
                .lastRefreshTime("2026-03-03 23:45:00")
                .manualInterventionRate(0.02)
                .regionData(Map.of("school_1001", Map.of("score", 92.0), "school_1002", Map.of("score", 96.5)))
                .build();
                
        redisTemplate.opsForValue().set("dashboard:heatmap:city", cityData);
        redisTemplate.opsForValue().set("dashboard:heatmap:district:201", districtData);
        
        return "Redis mock data injected successfully!";
    }
}
