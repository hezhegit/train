package com.hezhe.train.member.controller.admin;

import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.member.req.TicketQueryReq;
import com.hezhe.train.member.resp.TicketQueryResp;
import com.hezhe.train.member.service.TicketService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/ticket")
public class TicketAdminController {

    @Resource
    private TicketService ticketService;


    @GetMapping("/query-list")
    public Result queryList(@Valid TicketQueryReq req) {
        PageResp<TicketQueryResp> ticketQueryRespPageResp = ticketService.queryList(req);
        return Result.ok().data("content", ticketQueryRespPageResp);
    }
}
