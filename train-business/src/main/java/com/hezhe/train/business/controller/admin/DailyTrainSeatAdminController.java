package com.hezhe.train.business.controller.admin;

import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.business.req.DailyTrainSeatQueryReq;
import com.hezhe.train.business.req.DailyTrainSeatSaveReq;
import com.hezhe.train.business.resp.DailyTrainSeatQueryResp;
import com.hezhe.train.business.service.DailyTrainSeatService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/daily-train-seat")
public class DailyTrainSeatAdminController {

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody DailyTrainSeatSaveReq req) {
        dailyTrainSeatService.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid DailyTrainSeatQueryReq req) {
        PageResp<DailyTrainSeatQueryResp> dailyTrainSeatQueryRespPageResp = dailyTrainSeatService.queryList(req);
        return Result.ok().data("content", dailyTrainSeatQueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        dailyTrainSeatService.deleteById(id);
        return Result.ok();
    }
}
