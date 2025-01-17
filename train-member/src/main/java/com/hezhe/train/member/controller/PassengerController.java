package com.hezhe.train.member.controller;

import com.hezhe.train.common.resp.Result;
import com.hezhe.train.member.req.PassengerSaveReq;
import com.hezhe.train.member.service.PassengerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
