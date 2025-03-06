package com.hezhe.train.business.service;

import cn.hutool.core.date.DateTime;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.hezhe.train.business.domain.ConfirmOrder;
import com.hezhe.train.business.dto.ConfirmOrderMQDto;
import com.hezhe.train.business.enums.ConfirmOrderStatusEnum;
import com.hezhe.train.business.enums.RocketMQTopicEnum;
import com.hezhe.train.business.mapper.ConfirmOrderMapper;
import com.hezhe.train.business.req.ConfirmOrderDoReq;
import com.hezhe.train.business.req.ConfirmOrderTicketReq;
import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.exception.BusinessException;
import com.hezhe.train.common.resp.ResultCode;
import com.hezhe.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
        req.setMemberId(LoginMemberContext.getId());
        // 校验令牌余量
        boolean validSkToken = skTokenService.validSkToken(req.getDate(), req.getTrainCode(), LoginMemberContext.getId());
        if (validSkToken) {
            LOG.info("令牌校验通过");
        } else {
            LOG.info("令牌校验不通过");
            throw new BusinessException(ResultCode.CONFIRM_ORDER_SK_TOKEN_FAIL.getCode(), ResultCode.CONFIRM_ORDER_SK_TOKEN_FAIL.getMessage());
        }
        // 先保存订单=》再发MQ等待出票
        // 数据保存到订单表
        DateTime now = DateTime.now();
        Date date = req.getDate();
        String trainCode = req.getTrainCode();
        String start = req.getStart();
        String end = req.getEnd();
        List<ConfirmOrderTicketReq> tickets = req.getTickets();

        ConfirmOrder confirmOrder = new ConfirmOrder();
        confirmOrder.setId(SnowUtil.getSnowflakeNextId());
        confirmOrder.setMemberId(req.getMemberId());
        confirmOrder.setDate(date);
        confirmOrder.setTrainCode(trainCode);
        confirmOrder.setStart(start);
        confirmOrder.setEnd(end);
        confirmOrder.setDailyTrainTicketId(req.getDailyTrainTicketId());
        confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
        confirmOrder.setCreateTime(now);
        confirmOrder.setUpdateTime(now);
        confirmOrder.setTickets(JSON.toJSONString(tickets));
        confirmOrderMapper.insert(confirmOrder);





        // 锁：日期+车次
//        String lockKey = RedisKeyPreEnum.CONFIRM_ORDER + "-" + DateUtil.formatDate(req.getDate()) + "-" + req.getTrainCode();
//
//        // setIfAbsent: setnx
//        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey, 5, TimeUnit.SECONDS);
//        if (Boolean.TRUE.equals(setIfAbsent)) {
//            LOG.info("恭喜，抢到锁了！");
//        }else {
//            LOG.info("很遗憾，没抢到锁。");
//            throw new BusinessException(ResultCode.CONFIRM_ORDER_LOCK_FAIL.getCode(), ResultCode.CONFIRM_ORDER_LOCK_FAIL.getMessage());
//        }

        // mq 处理购票
        // 发送MQ排队购票
        ConfirmOrderMQDto confirmOrderMQDto = new ConfirmOrderMQDto();
        confirmOrderMQDto.setDate(req.getDate());
        confirmOrderMQDto.setTrainCode(req.getTrainCode());
        confirmOrderMQDto.setLogId(MDC.get("LOG_ID"));
        String reqJson = JSON.toJSONString(confirmOrderMQDto);
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
