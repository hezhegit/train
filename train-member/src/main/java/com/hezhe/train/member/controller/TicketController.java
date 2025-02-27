package com.hezhe.train.member.controller;

import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.member.req.TicketQueryReq;
import com.hezhe.train.member.resp.TicketQueryResp;
import com.hezhe.train.member.service.TicketService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    @Resource
    private TicketService ticketService;


    @GetMapping("/query-list")
    public Result queryList(@Valid TicketQueryReq req) {
        req.setMemberId(LoginMemberContext.getId());
        PageResp<TicketQueryResp> ticketQueryRespPageResp = ticketService.queryList(req);
        return Result.ok().data("content", ticketQueryRespPageResp);
    }
}
