package com.hezhe.train.business.controller;

import com.hezhe.train.business.req.ConfirmOrderDoReq;
import com.hezhe.train.business.service.BeforeConfirmOrderService;
import com.hezhe.train.business.service.ConfirmOrderService;
import com.hezhe.train.common.resp.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {

    @Resource
    private BeforeConfirmOrderService beforeConfirmOrderService;

    @Resource
    private ConfirmOrderService confirmOrderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderController.class);

    @PostMapping("/do")
    public Result doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) {
        // 图形验证码校验
        String imageCodeToken = req.getImageCodeToken();
        String imageCode = req.getImageCode();
        String imageCodeRedis = redisTemplate.opsForValue().get(imageCodeToken);
        LOG.info("从redis中获取到的验证码：{}", imageCodeRedis);
        if (ObjectUtils.isEmpty(imageCodeRedis)) {
            return Result.error().message("验证码已过期");
        }
        // 验证码校验，大小写忽略，提升体验，比如Oo Vv Ww容易混
        if (!imageCodeRedis.equalsIgnoreCase(imageCode)) {
            return Result.error().message("验证码不正确");
        } else {
            // 验证通过后，移除验证码
            redisTemplate.delete(imageCodeToken);
        }

        Long id = beforeConfirmOrderService.beforeDoConfirm(req);
        return Result.ok().data("id", String.valueOf(id));
    }

    @GetMapping("/query-line-count/{id}")
    public Result queryLineCount(@PathVariable Long id) {
        Integer count = confirmOrderService.queryLineCount(id);
        return Result.ok().data("count", count);
    }

}
