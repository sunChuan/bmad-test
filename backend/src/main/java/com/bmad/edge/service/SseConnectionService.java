package com.bmad.edge.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Story 2.3 - SseEmitter 集中连接管理与消息分发推送服务
 * 维持前端请求挂起的连接池并执行单向 Server Push 机制
 */
@Slf4j
@Service
public class SseConnectionService {

    // K：业务身份 (例如: District_201_UserX) 
    // V：长连接实例
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // SSE 默认空闲释放时间定为 30分钟
    private static final Long DEFAULT_TIMEOUT = 30 * 60 * 1000L;

    /**
     * 加入流并保持挂起
     */
    public SseEmitter createConnection(String clientId) {
        // 创建带有生命周期的流
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        
        // 绑定资源释放逻辑
        emitter.onCompletion(() -> {
            log.info("SSE 连接自然或主动断开, Client ID: {}", clientId);
            emitters.remove(clientId);
        });
        
        emitter.onTimeout(() -> {
            log.warn("SSE 连接已空闲超时, Client ID: {}", clientId);
            emitter.complete();
            emitters.remove(clientId);
        });
        
        emitter.onError((e) -> {
            log.error("SSE 连接传轮异常, Client ID: {}", clientId, e);
            emitter.complete();
            emitters.remove(clientId);
        });

        emitters.put(clientId, emitter);
        log.info("新设 SSE 通道建立成功, 当前活跃信道数: {}, 接入标识: {}", emitters.size(), clientId);
        return emitter;
    }

    /**
     * 单播特定用户 / Topic
     */
    public void sendToClient(String clientId, Object msg) {
        SseEmitter sseEmitter = emitters.get(clientId);
        if (sseEmitter != null) {
            try {
                // SseEmitter.event() 为标准构建语态，提供重试等属性配置
                sseEmitter.send(SseEmitter.event()
                        .name("alert") // Event name. JavaScript `addEventListener('alert')`
                        .data(msg));
            } catch (IOException e) {
                log.error("SSE 异常脱离, 试图向端点注入数据失败 Client: {}", clientId, e);
                emitters.remove(clientId);
            }
        }
    }

    /**
     * 进行全区县或跨区大面广播
     */
    public void broadcastToPrefix(String prefixFilter, Object msg) {
        emitters.forEach((key, emitter) -> {
            if (key.startsWith(prefixFilter)) {
                try {
                    emitter.send(SseEmitter.event().name("alert").data(msg));
                } catch (IOException e) {
                    emitters.remove(key);
                }
            }
        });
    }
}
