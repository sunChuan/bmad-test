package com.bmad.edge.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bmad.edge.common.Result;
import com.bmad.edge.entity.Article;
import com.bmad.edge.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cms/article")
public class ArticleController {

    @Autowired
    private ArticleMapper articleMapper;

    @PostMapping("/publish")
    public Result<Article> publish(@RequestBody Article article) {
        article.setPublishTime(LocalDateTime.now());
        // Mock Author Id
        article.setAuthorId(1L);
        articleMapper.insert(article);
        return Result.success(article);
    }
    
    @GetMapping("/list")
    public Result<List<Article>> list() {
        QueryWrapper<Article> qw = new QueryWrapper<>();
        qw.orderByDesc("publish_time");
        return Result.success(articleMapper.selectList(qw));
    }
}
