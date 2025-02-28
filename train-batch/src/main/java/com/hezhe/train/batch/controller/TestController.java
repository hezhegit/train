package com.hezhe.train.batch.controller;

import com.hezhe.train.batch.feign.BusinessFeign;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Resource
    private BusinessFeign businessFeign;

    @GetMapping("/hello")
    public String hello() {
        return " === BusinessFeign TEST! ==="+ businessFeign.hello();
    }
}
