package com.hezhe.train.business.controller.admin;

import com.hezhe.train.business.req.DailyTrainQueryReq;
import com.hezhe.train.business.req.DailyTrainSaveReq;
import com.hezhe.train.business.resp.DailyTrainQueryResp;
import com.hezhe.train.business.service.DailyTrainService;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/admin/daily-train")
public class DailyTrainAdminController {

    @Resource
    private DailyTrainService dailyTrainService;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody DailyTrainSaveReq req) {
        dailyTrainService.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid DailyTrainQueryReq req) {
        PageResp<DailyTrainQueryResp> dailyTrainQueryRespPageResp = dailyTrainService.queryList(req);
        return Result.ok().data("content", dailyTrainQueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        dailyTrainService.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/gen-daily/{date}")
    public Result genDaily(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        dailyTrainService.generateDaily(date);
        return Result.ok();
    }
}
