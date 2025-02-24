package com.hezhe.train.business.controller;

import com.hezhe.train.business.req.ConfirmOrderDoReq;
import com.hezhe.train.business.service.ConfirmOrderService;
import com.hezhe.train.common.resp.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {

    @Resource
    private ConfirmOrderService confirmOrderService;


    @PostMapping("/do")
    public Result doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) {
        confirmOrderService.doConfirm(req);
        return Result.ok();
    }

}
