package com.hezhe.train.business.controller.admin;

import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.business.req.TrainCarriageQueryReq;
import com.hezhe.train.business.req.TrainCarriageSaveReq;
import com.hezhe.train.business.resp.TrainCarriageQueryResp;
import com.hezhe.train.business.service.TrainCarriageService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/train-carriage")
public class TrainCarriageAdminController {

    @Resource
    private TrainCarriageService trainCarriageService;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody TrainCarriageSaveReq req) {
        trainCarriageService.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid TrainCarriageQueryReq req) {
        PageResp<TrainCarriageQueryResp> trainCarriageQueryRespPageResp = trainCarriageService.queryList(req);
        return Result.ok().data("content", trainCarriageQueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        trainCarriageService.deleteById(id);
        return Result.ok();
    }
}
