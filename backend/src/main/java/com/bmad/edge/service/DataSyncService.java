package com.bmad.edge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 模拟 XXL-JOB 数据同步与断点续传 (Story 3.1)
 */
@Service
public class DataSyncService {

    private static final Logger log = LoggerFactory.getLogger(DataSyncService.class);
    
    private int retryCount = 0;
    private final int MAX_RETRIES = 3;
    private final Random random = new Random();

    // 每天凌晨两点，或者为了测试改为每隔 2 分钟执行一次
    @Scheduled(fixedDelay = 120000) 
    public void syncDataFromCenter() {
        log.info("[XXL-JOB Mock] 开始尝试从大数据中心拉取增量跑批成绩... {}", LocalDateTime.now());
        
        try {
            // 模拟随机的网络毛刺或超时
            if (random.nextInt(10) < 3) { // 30% 概率阻断
                throw new RuntimeException("读取数据流超时 (Timeout > 10s)");
            }
            log.info("[XXL-JOB Mock] 拉取成功！正在 Upsert 到 MySQL 基础表...");
            // 清除重试计数
            retryCount = 0;
            
        } catch (Exception e) {
            retryCount++;
            log.error("[XXL-JOB Mock] 拉取失败: {} - 当前重试次数: {}", e.getMessage(), retryCount);
            
            if (retryCount >= MAX_RETRIES) {
                log.error("🚨 [ALARM] 连续 3 次抽取彻底失败！已触发短信/钉钉告警机制！任务刮起等待人工干预。");
                // 真实场景中此处会调起告警 API
                retryCount = 0; // 重置以防假死
            }
        }
    }
}
