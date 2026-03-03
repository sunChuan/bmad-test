package com.bmad.edge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学校实体类（示例）。
 * 演示多租户拦截器的行级隔离效果。
 * 
 * 表中 district_id 为租户隔离字段，由 TenantLineInnerInterceptor 自动注入过滤条件。
 */
@Data
@TableName("school")
public class School {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学校名称 */
    private String name;

    /** 学校代码 */
    private String code;

    /** 所属区县 ID（租户隔离字段，TenantLineInnerInterceptor 自动拼装） */
    private Long districtId;

    /** 所属区县名称 */
    private String districtName;

    /** 学校类型：PRIMARY(小学)/MIDDLE(初中)/HIGH(高中) */
    private String schoolType;

    /** 在校学生数 */
    private Integer studentCount;

    /** 教职工人数 */
    private Integer teacherCount;

    /** 综合增值评价得分 */
    private Double assessmentScore;

    /** 预警状态：NORMAL(正常)/WARNING(预警)/CRITICAL(严重) */
    private String alertStatus;

    /** 逻辑删除标志：0-未删除，1-已删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
