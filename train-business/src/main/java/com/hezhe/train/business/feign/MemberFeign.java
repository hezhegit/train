package com.hezhe.train.business.feign;

import com.hezhe.train.common.req.MemberTicketReq;
import com.hezhe.train.common.resp.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

// @FeignClient("member")
@FeignClient(name = "member", url = "http://127.0.0.1:8001")
public interface MemberFeign {

    @GetMapping("/member/feign/ticket/save")
    Result save(@RequestBody MemberTicketReq req);

}
