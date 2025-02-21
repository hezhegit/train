package com.hezhe.train.business.controller;

import com.hezhe.train.business.resp.StationQueryResp;
import com.hezhe.train.business.service.StationService;
import com.hezhe.train.common.resp.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/station")
public class StationController {

    @Resource
    private StationService stationService;

    @GetMapping("/query-all")
    public Result queryList() {
        List<StationQueryResp> stationQueryResps = stationService.queryAll();
        return Result.ok().data("content", stationQueryResps);
    }
}
