package com.hezhe.train.member.controller;

import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.member.req.PassengerQueryReq;
import com.hezhe.train.member.req.PassengerSaveReq;
import com.hezhe.train.member.resp.PassengerQueryResp;
import com.hezhe.train.member.service.PassengerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/passenger")
public class PassengerController {

    @Resource
    private PassengerService passengerService;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody PassengerSaveReq req) {
        passengerService.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid PassengerQueryReq req) {
        req.setMemberId(LoginMemberContext.getId());
        PageResp<PassengerQueryResp> passengerQueryRespPageResp = passengerService.queryList(req);
        return Result.ok().data("content", passengerQueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        passengerService.deleteById(id);
        return Result.ok();
    }
}
