package com.hezhe.train.business.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.hezhe.train.business.enums.RedisKeyPreEnum;
import com.hezhe.train.business.enums.RocketMQTopicEnum;
import com.hezhe.train.business.mapper.ConfirmOrderMapper;
import com.hezhe.train.business.req.ConfirmOrderDoReq;
import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.exception.BusinessException;
import com.hezhe.train.common.resp.ResultCode;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class BeforeConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(BeforeConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Autowired
    private SkTokenService skTokenService;

     @Resource
     public RocketMQTemplate rocketMQTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private ConfirmOrderService confirmOrderService;

    @SentinelResource(value = "beforeDoConfirm", blockHandler = "beforeDoConfirmBlock")
    public void beforeDoConfirm(ConfirmOrderDoReq req) {
        // 校验令牌余量
        boolean validSkToken = skTokenService.validSkToken(req.getDate(), req.getTrainCode(), LoginMemberContext.getId());
        if (validSkToken) {
            LOG.info("令牌校验通过");
        } else {
            LOG.info("令牌校验不通过");
            throw new BusinessException(ResultCode.CONFIRM_ORDER_SK_TOKEN_FAIL.getCode(), ResultCode.CONFIRM_ORDER_SK_TOKEN_FAIL.getMessage());
        }


        // 锁：日期+车次
        String lockKey = RedisKeyPreEnum.CONFIRM_ORDER + "-" + DateUtil.formatDate(req.getDate()) + "-" + req.getTrainCode();

        // setIfAbsent: setnx
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey, 5, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(setIfAbsent)) {
            LOG.info("恭喜，抢到锁了！");
        }else {
            LOG.info("很遗憾，没抢到锁。");
            throw new BusinessException(ResultCode.CONFIRM_ORDER_LOCK_FAIL.getCode(), ResultCode.CONFIRM_ORDER_LOCK_FAIL.getMessage());
        }

        // mq 处理购票
        String reqJson = JSON.toJSONString(req);
         LOG.info("排队购票，发送mq开始，消息：{}", reqJson);
         rocketMQTemplate.convertAndSend(RocketMQTopicEnum.CONFIRM_ORDER.getCode(), reqJson);
         LOG.info("排队购票，发送mq结束");

    }

    /**
     * 降级方法，需包含限流方法的所有参数和BlockException参数
     * @param req
     * @param e
     */
    public void beforeDoConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
        LOG.info("购票请求被限流：{}", req);
        throw new BusinessException(ResultCode.CONFIRM_ORDER_FLOW_EXCEPTION.getCode(), ResultCode.CONFIRM_ORDER_FLOW_EXCEPTION.getMessage());

    }
}
