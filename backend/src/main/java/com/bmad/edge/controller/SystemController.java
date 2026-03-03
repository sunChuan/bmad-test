package com.bmad.edge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bmad.edge.common.Result;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    @GetMapping("/version")
    public Result<String> getVersion() {
        return Result.success("1.0.0");
    }
}
