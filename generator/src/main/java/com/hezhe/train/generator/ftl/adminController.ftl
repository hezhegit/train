package com.hezhe.train.${module}.controller.admin;

import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.${module}.req.${Domain}QueryReq;
import com.hezhe.train.${module}.req.${Domain}SaveReq;
import com.hezhe.train.${module}.resp.${Domain}QueryResp;
import com.hezhe.train.${module}.service.${Domain}Service;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/${do_main}")
public class ${Domain}AdminController {

    @Resource
    private ${Domain}Service ${domain}Service;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody ${Domain}SaveReq req) {
        ${domain}Service.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid ${Domain}QueryReq req) {
        PageResp<${Domain}QueryResp> ${domain}QueryRespPageResp = ${domain}Service.queryList(req);
        return Result.ok().data("content", ${domain}QueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        ${domain}Service.deleteById(id);
        return Result.ok();
    }
}
