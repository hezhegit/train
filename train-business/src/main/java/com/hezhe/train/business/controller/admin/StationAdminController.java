package com.hezhe.train.business.controller.admin;

import com.hezhe.train.business.req.StationQueryReq;
import com.hezhe.train.business.req.StationSaveReq;
import com.hezhe.train.business.resp.StationQueryResp;
import com.hezhe.train.business.service.StationService;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/station")
public class StationAdminController {

    @Resource
    private StationService stationService;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody StationSaveReq req) {
        stationService.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid StationQueryReq req) {
        PageResp<StationQueryResp> stationQueryRespPageResp = stationService.queryList(req);
        return Result.ok().data("content", stationQueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        stationService.deleteById(id);
        return Result.ok();
    }
}
