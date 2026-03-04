package com.bmad.edge.service;

import com.bmad.edge.dto.SandboxConfigDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟沙箱引擎 (Story 3.4)
 * 使用 Spring @Async 模拟跨服跑批及进度推演
 */
@Service
public class SandboxEngineService {

    private static final Logger log = LoggerFactory.getLogger(SandboxEngineService.class);

    // 模拟 Redis Hash，存储 Task ID -> 完成度 (0-100)
    private final Map<String, Integer> taskProgressMap = new ConcurrentHashMap<>();

    // 存储当前暂存跑通的最优配置环境（待确认发版使用）
    private String completedPendingTaskId = null;

    /**
     * 发起推演。为了演示效果，每隔半秒刷新一定进度。
     */
    @Async
    public void startSimulation(String taskId, SandboxConfigDTO config) {
        log.info("[Python Sandbox Mock] 接受到任务 ID {}，启动独立进程推演全量 150 所学校...", taskId);
        taskProgressMap.put(taskId, 0);
        
        try {
            for (int i = 1; i <= 10; i++) {
                // 模拟耗时运算
                Thread.sleep(800);
                taskProgressMap.put(taskId, i * 10);
                log.info("[Python Sandbox Mock] 任务 {} 当前进度: {}%", taskId, i * 10);
            }
            log.info("[Python Sandbox Mock] 任务 {} 模拟完成！生成全区新版排名与基线...", taskId);
            
            this.completedPendingTaskId = taskId;
        } catch (InterruptedException e) {
            log.error("推演中断", e);
            taskProgressMap.put(taskId, -1); // -1 标记失败
        }
    }

    public Integer getProgress(String taskId) {
        return taskProgressMap.getOrDefault(taskId, -1);
    }
    
    public String getCompletedPendingTaskId() {
        return this.completedPendingTaskId;
    }
    
    public void clearPendingTask() {
        this.completedPendingTaskId = null;
    }
}
