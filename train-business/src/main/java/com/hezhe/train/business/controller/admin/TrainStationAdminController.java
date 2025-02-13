package com.hezhe.train.business.controller.admin;

import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.business.req.TrainStationQueryReq;
import com.hezhe.train.business.req.TrainStationSaveReq;
import com.hezhe.train.business.resp.TrainStationQueryResp;
import com.hezhe.train.business.service.TrainStationService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/train-station")
public class TrainStationAdminController {

    @Resource
    private TrainStationService trainStationService;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody TrainStationSaveReq req) {
        trainStationService.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid TrainStationQueryReq req) {
        PageResp<TrainStationQueryResp> trainStationQueryRespPageResp = trainStationService.queryList(req);
        return Result.ok().data("content", trainStationQueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        trainStationService.deleteById(id);
        return Result.ok();
    }
}
