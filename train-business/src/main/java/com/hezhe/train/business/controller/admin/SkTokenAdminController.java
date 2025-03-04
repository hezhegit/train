package com.hezhe.train.business.controller.admin;

import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.business.req.SkTokenQueryReq;
import com.hezhe.train.business.req.SkTokenSaveReq;
import com.hezhe.train.business.resp.SkTokenQueryResp;
import com.hezhe.train.business.service.SkTokenService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/sk-token")
public class SkTokenAdminController {

    @Resource
    private SkTokenService skTokenService;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody SkTokenSaveReq req) {
        skTokenService.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid SkTokenQueryReq req) {
        PageResp<SkTokenQueryResp> skTokenQueryRespPageResp = skTokenService.queryList(req);
        return Result.ok().data("content", skTokenQueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        skTokenService.deleteById(id);
        return Result.ok();
    }
}
