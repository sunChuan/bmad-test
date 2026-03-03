package com.bmad.edge.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bmad.edge.entity.School;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校数据访问层。
 * 继承 MyBatis-Plus BaseMapper，自动享有 CRUD 功能。
 * 所有查询均受 TenantLineInnerInterceptor 保护，自动追加 district_id 过滤条件。
 */
@Mapper
public interface SchoolMapper extends BaseMapper<School> {
    // BaseMapper 已提供 selectList/selectById/insert/update/delete 等标准 CRUD
    // TenantLineInnerInterceptor 会在执行时自动拼装 WHERE district_id = ?
}
