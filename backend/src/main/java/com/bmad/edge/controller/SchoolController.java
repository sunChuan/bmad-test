package com.bmad.edge.controller;

import com.bmad.edge.common.Result;
import com.bmad.edge.entity.School;
import com.bmad.edge.repository.SchoolMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学校业务控制器。
 * 用于演示 MyBatis-Plus 多租户拦截器的行级隔离效果。
 */
@RestController
@RequestMapping("/api/v1/schools")
public class SchoolController {

    @Autowired
    private SchoolMapper schoolMapper;

    /**
     * 获取学校列表。
     * 实际执行时，MyBatis-Plus 会自动根据当前登录用户的 districtId 注入过滤条件。
     * - 区县局长：只能看到本区的学校
     * - 市管理员：能看到全市所有学校
     */
    @GetMapping
    public Result<List<School>> list() {
        // 开发者只需编写简单的 selectList(null)
        // 拦截器会自动补全 WHERE district_id = ?
        List<School> schools = schoolMapper.selectList(null);
        return Result.success(schools);
    }
}
