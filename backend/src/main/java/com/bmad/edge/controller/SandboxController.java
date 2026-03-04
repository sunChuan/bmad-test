package com.bmad.edge.controller;

import com.bmad.edge.annotation.Audit;
import com.bmad.edge.common.Result;
import com.bmad.edge.dto.SandboxConfigDTO;
import com.bmad.edge.service.SandboxEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sandbox")
public class SandboxController {

    @Autowired
    private SandboxEngineService sandboxEngineService;

    // 3.3 把配置推入沙箱队列
    @PostMapping("/config")
    @Audit(actionType = "SANDBOX_START", targetEntity = "ModelConfig")
    public Result<Map<String, String>> submitConfig(@RequestBody SandboxConfigDTO config) {
        String taskId = UUID.randomUUID().toString();
        sandboxEngineService.startSimulation(taskId, config);
        
        Map<String, String> data = new HashMap<>();
        data.put("taskId", taskId);
        data.put("status", "QUEUED");
        return Result.success(data);
    }

    // 3.4 前端定时长轮询接口
    @GetMapping("/task/{taskId}/progress")
    public Result<Integer> checkProgress(@PathVariable String taskId) {
        Integer progress = sandboxEngineService.getProgress(taskId);
        if (progress == -1) {
            return Result.error(500, "试跑队列断流失效或查无此任务");
        }
        return Result.success(progress);
    }

    // 3.5 审核完成，一键推入生产环境覆盖大屏主线
    @PostMapping("/publish-baseline")
    @Audit(actionType = "PUBLISH_BASELINE", targetEntity = "DashboardEngine")
    public Result<String> publishBaseline() {
        String pendingId = sandboxEngineService.getCompletedPendingTaskId();
        if (pendingId == null) {
             return Result.error(400, "目前沙箱中并没有已经完成且等待发布的有效试算数据模型");
        }
        // 模拟清洗 Redis 旧热点，更新元数据指针
        sandboxEngineService.clearPendingTask();
        return Result.success("部署成功，V2 大基线已覆盖大屏生产流！");
    }
}
