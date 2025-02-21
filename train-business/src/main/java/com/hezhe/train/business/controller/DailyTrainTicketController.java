package com.hezhe.train.business.controller;

import com.hezhe.train.business.req.DailyTrainTicketQueryReq;
import com.hezhe.train.business.resp.DailyTrainTicketQueryResp;
import com.hezhe.train.business.service.DailyTrainTicketService;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/daily-train-ticket")
public class DailyTrainTicketController {

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;

    @GetMapping("/query-list")
    public Result queryList(@Valid DailyTrainTicketQueryReq req) {
        PageResp<DailyTrainTicketQueryResp> dailyTrainTicketQueryRespPageResp = dailyTrainTicketService.queryList(req);
        return Result.ok().data("content", dailyTrainTicketQueryRespPageResp);
    }

}
