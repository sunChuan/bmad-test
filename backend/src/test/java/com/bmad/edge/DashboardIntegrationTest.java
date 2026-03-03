package com.bmad.edge;

import com.bmad.edge.dto.HeatMapDataDTO;
import com.bmad.edge.security.SecurityUser;
import com.bmad.edge.service.DashboardService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

@SpringBootTest
public class DashboardIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    public void setupMockRedis() {
        HeatMapDataDTO cityData = HeatMapDataDTO.builder()
                .lastRefreshTime("2026-03-03 23:45:00").manualInterventionRate(0.08)
                .regionData(Map.of("district_201", Map.of("score", 95.5), "district_301", Map.of("score", 88.0))).build();
        HeatMapDataDTO districtData = HeatMapDataDTO.builder()
                .lastRefreshTime("2026-03-03 23:45:00").manualInterventionRate(0.02)
                .regionData(Map.of("school_1001", Map.of("score", 92.0), "school_1002", Map.of("score", 96.5))).build();
        
        redisTemplate.opsForValue().set("dashboard:heatmap:city", cityData);
        redisTemplate.opsForValue().set("dashboard:heatmap:district:201", districtData);
    }

    @Test
    public void testCityAdminAccess() {
        SecurityUser user = new SecurityUser("cityAdmin", "ROLE_CITY_ADMIN", 100L, null, null);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        HeatMapDataDTO data = dashboardService.getMacroHeatMap();
        Assertions.assertEquals(0.08, data.getManualInterventionRate());
        System.out.println("City Admin 测试成功通过集成！得到干预比例：" + data.getManualInterventionRate());
    }
    
    @Test
    public void testDistrictDirectorAccess() {
        SecurityUser user = new SecurityUser("distAdmin", "ROLE_DISTRICT_DIRECTOR", 200L, 201L, null);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        HeatMapDataDTO data = dashboardService.getMacroHeatMap();
        Assertions.assertEquals(0.02, data.getManualInterventionRate());
        System.out.println("District Director 201 测试成功通过集成！得到干预比例：" + data.getManualInterventionRate());
    }

    @Test
    public void testDistrictDirectorNoCacheFallback() {
        SecurityUser user = new SecurityUser("distAdmin999", "ROLE_DISTRICT_DIRECTOR", 200L, 999L, null);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        HeatMapDataDTO data = dashboardService.getMacroHeatMap();
        Assertions.assertNotNull(data);
        Assertions.assertEquals("N/A", data.getLastRefreshTime());
        System.out.println("No Cache Fallback 测试成功！拿到空数据集: " + data.getRegionData());
    }
}
