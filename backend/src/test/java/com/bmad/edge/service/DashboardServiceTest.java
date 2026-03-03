package com.bmad.edge.service;

import com.bmad.edge.dto.HeatMapDataDTO;
import com.bmad.edge.security.SecurityUser;
import com.bmad.edge.config.TenantContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(Long userId, Long districtId, Long schoolId, String role) {
        SecurityUser user = new SecurityUser("test", role, districtId != null ? districtId : schoolId, districtId, schoolId);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @Test
    void testGetMacroHeatMap_CityAdmin_HitCache() {
        mockSecurityContext(1L, null, null, "city_admin");
        
        HeatMapDataDTO mockData = HeatMapDataDTO.builder()
                .lastRefreshTime("2026-03-03 10:00:00")
                .manualInterventionRate(0.01)
                .regionData(Map.of("district_201", 95.5))
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("dashboard:heatmap:city")).thenReturn(mockData);

        HeatMapDataDTO result = dashboardService.getMacroHeatMap();

        assertNotNull(result);
        assertEquals("2026-03-03 10:00:00", result.getLastRefreshTime());
        assertEquals(0.01, result.getManualInterventionRate());
        verify(redisTemplate, times(1)).opsForValue();
    }

    @Test
    void testGetMacroHeatMap_DistrictDirector_HitCache() {
        mockSecurityContext(2L, 201L, null, "district_director");

        HeatMapDataDTO mockData = HeatMapDataDTO.builder()
                .lastRefreshTime("2026-03-03 11:00:00")
                .manualInterventionRate(0.05)
                .regionData(Map.of("school_1001", 88.0))
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("dashboard:heatmap:district:201")).thenReturn(mockData);

        HeatMapDataDTO result = dashboardService.getMacroHeatMap();

        assertNotNull(result);
        assertEquals("2026-03-03 11:00:00", result.getLastRefreshTime());
        assertEquals(0.05, result.getManualInterventionRate());
    }

    @Test
    void testGetMacroHeatMap_FallbackDueToNoCache() {
        mockSecurityContext(1L, null, null, "city_admin");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        HeatMapDataDTO result = dashboardService.getMacroHeatMap();

        assertNotNull(result);
        assertEquals("N/A", result.getLastRefreshTime());
        assertEquals(0.0, result.getManualInterventionRate());
    }

    @Test
    void testGetMacroHeatMap_FallbackDueToUnauthorizedRole() {
        mockSecurityContext(3L, 201L, 1001L, "school_principal"); // 无权角色

        HeatMapDataDTO result = dashboardService.getMacroHeatMap();

        assertNotNull(result);
        assertEquals("N/A", result.getLastRefreshTime());
        verify(redisTemplate, never()).opsForValue();
    }
}
