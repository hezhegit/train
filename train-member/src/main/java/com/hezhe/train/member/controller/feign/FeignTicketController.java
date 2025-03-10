package com.hezhe.train.member.controller.feign;

import com.hezhe.train.common.req.MemberTicketReq;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.member.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

;

@RestController
@RequestMapping("/feign/ticket")
public class FeignTicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/save")
    public Result save(@Valid @RequestBody MemberTicketReq req) throws Exception {
        ticketService.save(req);
        return Result.ok();
    }

}
