package com.bmad.edge.service;

import com.bmad.edge.dto.HeatMapDataDTO;
import com.bmad.edge.config.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String HEATMAP_CITY_KEY = "dashboard:heatmap:city";
    private static final String HEATMAP_DISTRICT_KEY_PREFIX = "dashboard:heatmap:district:";

    /**
     * 根据当前用户 Token 的层级标签拉取对应可见范围的热力图缓存。
     * CQRS 架构下，此链路纯读 Redis 不查 DB。
     */
    public HeatMapDataDTO getMacroHeatMap() {
        String rolePrefix = TenantContextHolder.getCurrentRole();
        Long districtId = TenantContextHolder.getCurrentTenantId();
        String targetRedisKey;

        // 根据角色构造细粒度的 Redis Key
        if ("city_admin".equals(rolePrefix)) {
            targetRedisKey = HEATMAP_CITY_KEY;
        } else if ("district_director".equals(rolePrefix)) {
            if (districtId == null) {
                log.warn("区县负责人缺少 districtId，将降级处理边界。");
                return getFallbackEmptyData();
            }
            targetRedisKey = HEATMAP_DISTRICT_KEY_PREFIX + districtId;
        } else {
            // 普通权限或无权查看宏观维度，采取优雅降级或返回空壳，避免系统报错
            log.warn("越权或无宏观视角角色尝试访问热力图大屏：{}", rolePrefix);
            return getFallbackEmptyData();
        }

        log.debug("尝试命中 Dashboard Redis Key: {}", targetRedisKey);
        
        try {
            Object cachedData = redisTemplate.opsForValue().get(targetRedisKey);
            if (cachedData != null) {
                // 利用 Jackson Config 进行强转映射
                // *注意: 由于 GenericJackson2JsonRedisSerializer 的限制，这里直接转换可能含有 LinkedHashMap 嵌套
                // 后续可通过 ObjectMapper 二次反演，为保持网关最高性能，对于单纯的 API 透传可以不写死类型强转。
                // 若明确要还原回 HeatMapDataDTO，可依靠配置里的 ValueSerializer 自动绑定。
                return (HeatMapDataDTO) cachedData;
            }
        } catch (Exception e) {
            log.error("在提取 Redis 缓存片段时发生解包错误或通讯失败: {}", e.getMessage());
        }

        // 容错兜底：当缓存在计算真空期或 Redis 宕机时，返回空数据面板而非报错，满足等保系统的鲁棒性要求
        log.warn("无法从 {} 命中有效的热力图结构，返回空壳降级对象。", targetRedisKey);
        return getFallbackEmptyData();
    }

    private HeatMapDataDTO getFallbackEmptyData() {
        return HeatMapDataDTO.builder()
                .lastRefreshTime("N/A")
                .manualInterventionRate(0.0)
                .regionData(new HashMap<>())
                .build();
    }
}
