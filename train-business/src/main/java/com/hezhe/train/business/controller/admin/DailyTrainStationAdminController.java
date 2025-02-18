package com.hezhe.train.business.controller.admin;

import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.business.req.DailyTrainStationQueryReq;
import com.hezhe.train.business.req.DailyTrainStationSaveReq;
import com.hezhe.train.business.resp.DailyTrainStationQueryResp;
import com.hezhe.train.business.service.DailyTrainStationService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/daily-train-station")
public class DailyTrainStationAdminController {

    @Resource
    private DailyTrainStationService dailyTrainStationService;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody DailyTrainStationSaveReq req) {
        dailyTrainStationService.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid DailyTrainStationQueryReq req) {
        PageResp<DailyTrainStationQueryResp> dailyTrainStationQueryRespPageResp = dailyTrainStationService.queryList(req);
        return Result.ok().data("content", dailyTrainStationQueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        dailyTrainStationService.deleteById(id);
        return Result.ok();
    }
}
