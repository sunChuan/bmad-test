package com.bmad.edge.aspect;

import com.bmad.edge.annotation.Audit;
import com.bmad.edge.entity.AuditLog;
import com.bmad.edge.mapper.AuditLogMapper;
import com.bmad.edge.security.JwtUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 审计日志切面
 * 拦截所有被 @Audit 标记的方法，异步或同步保存修改记录
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private JwtUtils jwtUtils;

    @AfterReturning(value = "@annotation(auditAnnotation)", returning = "result")
    public void recordAuditLog(JoinPoint joinPoint, Audit auditAnnotation, Object result) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setActionType(auditAnnotation.actionType());
            auditLog.setTargetEntity(auditAnnotation.targetEntity());
            auditLog.setOperateTime(LocalDateTime.now());

            // 尝试获取操作人信息
            String token = extractToken(request);
            if (token != null) {
                auditLog.setOperator(jwtUtils.getUsernameFromToken(token));
            } else {
                auditLog.setOperator("SYSTEM_OR_UNKNOWN");
            }

            // 此处通常需要更复杂的逻辑比对前后对象，此处仅以传入参数作为 newValue 示例
            Object[] args = joinPoint.getArgs();
            auditLog.setNewValue(Arrays.toString(args));
            auditLog.setOriginalValue("N/A - Mocked Extractor");
            
            // 为了模拟，targetId 取第一个参数如果是个数字
            if (args.length > 0 && args[0] instanceof Long) {
                auditLog.setTargetId((Long) args[0]);
            } else {
                auditLog.setTargetId(0L);
            }

            auditLogMapper.insert(auditLog);
            log.info("审计日志已成功落地: {}", auditLog);
        } catch (Exception e) {
            log.error("保存审计日志失败: {}", e.getMessage());
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
