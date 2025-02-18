package com.hezhe.train.business.controller.admin;

import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.business.req.DailyTrainCarriageQueryReq;
import com.hezhe.train.business.req.DailyTrainCarriageSaveReq;
import com.hezhe.train.business.resp.DailyTrainCarriageQueryResp;
import com.hezhe.train.business.service.DailyTrainCarriageService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/daily-train-carriage")
public class DailyTrainCarriageAdminController {

    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody DailyTrainCarriageSaveReq req) {
        dailyTrainCarriageService.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid DailyTrainCarriageQueryReq req) {
        PageResp<DailyTrainCarriageQueryResp> dailyTrainCarriageQueryRespPageResp = dailyTrainCarriageService.queryList(req);
        return Result.ok().data("content", dailyTrainCarriageQueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        dailyTrainCarriageService.deleteById(id);
        return Result.ok();
    }
}
