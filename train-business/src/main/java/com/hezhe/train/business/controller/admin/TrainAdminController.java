package com.hezhe.train.business.controller.admin;

import com.hezhe.train.business.req.TrainQueryReq;
import com.hezhe.train.business.req.TrainSaveReq;
import com.hezhe.train.business.resp.TrainQueryResp;
import com.hezhe.train.business.service.TrainService;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/train")
public class TrainAdminController {

    @Resource
    private TrainService trainService;


    @PostMapping("/save")
    public Result save(@Valid @RequestBody TrainSaveReq req) {
        trainService.save(req);
        return Result.ok();
    }

    @GetMapping("/query-list")
    public Result queryList(@Valid TrainQueryReq req) {
        PageResp<TrainQueryResp> trainQueryRespPageResp = trainService.queryList(req);
        return Result.ok().data("content", trainQueryRespPageResp);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        trainService.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/query-all")
    public Result queryList() {
        List<TrainQueryResp> trainQueryResps = trainService.queryAll();
        return Result.ok().data("content", trainQueryResps);
    }
}
