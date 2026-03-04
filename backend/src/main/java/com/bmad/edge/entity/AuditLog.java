package com.bmad.edge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("edu_audit_log")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String operator;
    private String actionType; // e.g., UPDATE, INSERT
    private String targetEntity;
    private Long targetId;
    private String originalValue;
    private String newValue;
    private LocalDateTime operateTime;
}
