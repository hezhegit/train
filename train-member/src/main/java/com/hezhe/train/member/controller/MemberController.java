package com.hezhe.train.member.controller;

import com.hezhe.train.common.resp.Result;
import com.hezhe.train.member.req.MemberRegisterReq;
import com.hezhe.train.member.req.MemberSendCodeReq;
import com.hezhe.train.member.service.MemberService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Resource
    private MemberService memberService;

    @GetMapping("/count")
    public Result hello() {
        int count = memberService.count();
        return Result.ok().data("count", count);
    }

    @PostMapping("/register")
    public Result register(@Valid MemberRegisterReq req) {
        long register = memberService.register(req);
        return Result.ok().data("register", register);
    }

    @PostMapping("/send-code")
    public Result sendCode(@Valid MemberSendCodeReq req) {
        memberService.sendCode(req);
        return Result.ok();
    }
}
