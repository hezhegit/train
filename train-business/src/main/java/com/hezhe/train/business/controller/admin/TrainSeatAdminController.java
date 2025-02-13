package com.hezhe.train.business.controller.admin;

import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.business.req.TrainSeatQueryReq;
import com.hezhe.train.business.req.TrainSeatSaveReq;
import com.hezhe.train.business.resp.TrainSeatQueryResp;
import com.hezhe.train.business.service.TrainSeatService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/train-seat")
public class TrainSeatAdminController {

    @Resource
    private TrainSeatService trainSeatService;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody TrainSeatSaveReq req) {
        trainSeatService.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid TrainSeatQueryReq req) {
        PageResp<TrainSeatQueryResp> trainSeatQueryRespPageResp = trainSeatService.queryList(req);
        return Result.ok().data("content", trainSeatQueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        trainSeatService.deleteById(id);
        return Result.ok();
    }
}
