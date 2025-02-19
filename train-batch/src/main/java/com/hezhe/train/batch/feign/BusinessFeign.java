package com.hezhe.train.batch.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

//@FeignClient("business")
@FeignClient(name = "business", url = "http://127.0.0.1:8002/business") // 无需配置中心的调用方法
public interface BusinessFeign {

    @GetMapping("/hello")
    String hello();
}
