package com.hezhe.train.business.controller.admin;

import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.business.req.ConfirmOrderQueryReq;
import com.hezhe.train.business.req.ConfirmOrderDoReq;
import com.hezhe.train.business.resp.ConfirmOrderQueryResp;
import com.hezhe.train.business.service.ConfirmOrderService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/confirm-order")
public class ConfirmOrderAdminController {

    @Resource
    private ConfirmOrderService confirmOrderService;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody ConfirmOrderDoReq req) {
        confirmOrderService.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid ConfirmOrderQueryReq req) {
        PageResp<ConfirmOrderQueryResp> confirmOrderQueryRespPageResp = confirmOrderService.queryList(req);
        return Result.ok().data("content", confirmOrderQueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        confirmOrderService.deleteById(id);
        return Result.ok();
    }
}
