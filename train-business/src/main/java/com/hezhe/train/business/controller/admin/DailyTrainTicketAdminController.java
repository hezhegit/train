package com.hezhe.train.business.controller.admin;

import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.business.req.DailyTrainTicketQueryReq;
import com.hezhe.train.business.req.DailyTrainTicketSaveReq;
import com.hezhe.train.business.resp.DailyTrainTicketQueryResp;
import com.hezhe.train.business.service.DailyTrainTicketService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/daily-train-ticket")
public class DailyTrainTicketAdminController {

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody DailyTrainTicketSaveReq req) {
        dailyTrainTicketService.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid DailyTrainTicketQueryReq req) {
        PageResp<DailyTrainTicketQueryResp> dailyTrainTicketQueryRespPageResp = dailyTrainTicketService.queryList(req);
        return Result.ok().data("content", dailyTrainTicketQueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        dailyTrainTicketService.deleteById(id);
        return Result.ok();
    }
}
