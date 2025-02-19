package com.hezhe.train.batch.feign;

import com.hezhe.train.common.resp.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;

//@FeignClient("business")
@FeignClient(name = "business", url = "http://127.0.0.1:8002/business") // 无需配置中心的调用方法
public interface BusinessFeign {

    @GetMapping("/hello")
    String hello();

    @GetMapping("/admin/daily-train/gen-daily/{date}")
    Result genDaily(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date);
}
