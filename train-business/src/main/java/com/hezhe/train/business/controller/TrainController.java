package com.hezhe.train.business.controller;

import com.hezhe.train.business.resp.TrainQueryResp;
import com.hezhe.train.business.service.TrainSeatService;
import com.hezhe.train.business.service.TrainService;
import com.hezhe.train.common.resp.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/train")
public class TrainController {

    @Resource
    private TrainService trainService;

    @Resource
    private TrainSeatService trainSeatService;


    @GetMapping("/query-all")
    public Result queryList() {
        List<TrainQueryResp> trainQueryResps = trainService.queryAll();
        return Result.ok().data("content", trainQueryResps);
    }


}
