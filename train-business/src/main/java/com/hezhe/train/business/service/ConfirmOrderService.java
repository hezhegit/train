package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.business.domain.*;
import com.hezhe.train.business.enums.ConfirmOrderStatusEnum;
import com.hezhe.train.business.enums.SeatColEnum;
import com.hezhe.train.business.enums.SeatTypeEnum;
import com.hezhe.train.business.mapper.ConfirmOrderMapper;
import com.hezhe.train.business.req.ConfirmOrderDoReq;
import com.hezhe.train.business.req.ConfirmOrderQueryReq;
import com.hezhe.train.business.req.ConfirmOrderTicketReq;
import com.hezhe.train.business.resp.ConfirmOrderQueryResp;
import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.exception.BusinessException;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.ResultCode;
import com.hezhe.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;

    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;
    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    @Resource
    private AfterConfirmOrderService afterConfirmOrderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    @Resource
    private SkTokenService skTokenService;



    public void save(ConfirmOrderDoReq req) {
        DateTime now = DateTime.now();
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);

        if (ObjectUtil.isNull(confirmOrder.getId())) {
            // 新增
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        }else {
            confirmOrder.setUpdateTime(now);
            // 空值 也会更新
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);
            // 空值 保留 原纪录值
//            confirmOrderMapper.updateByPrimaryKeySelective(confirmOrder);
        }



    }

    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req) {
        ConfirmOrderExample example = new ConfirmOrderExample();
        // id 倒序
        example.setOrderByClause("id desc");
        ConfirmOrderExample.Criteria criteria = example.createCriteria();
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<ConfirmOrder> confirmOrders = confirmOrderMapper.selectByExample(example);
        // select count
        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrders);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<ConfirmOrderQueryResp> confirmOrderQueryResps = BeanUtil.copyToList(confirmOrders, ConfirmOrderQueryResp.class);
        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(confirmOrderQueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        confirmOrderMapper.deleteByPrimaryKey(id);
    }

    @SentinelResource(value = "doConfirm", blockHandler = "doConfirmBlock")
    public void doConfirm(ConfirmOrderDoReq req) {

         // 校验令牌余量
         boolean validSkToken = skTokenService.validSkToken(req.getDate(), req.getTrainCode(), LoginMemberContext.getId());
         if (validSkToken) {
             LOG.info("令牌校验通过");
         } else {
             LOG.info("令牌校验不通过");
            throw new BusinessException(ResultCode.CONFIRM_ORDER_SK_TOKEN_FAIL.getCode(), ResultCode.CONFIRM_ORDER_SK_TOKEN_FAIL.getMessage());
         }


        // 锁：日期+车次
        String lockKey = req.getDate() + "-" + req.getTrainCode();
        // setIfAbsent: setnx
//        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey, 5, TimeUnit.SECONDS);
//        if (setIfAbsent) {
//            LOG.info("恭喜，抢到锁了！");
//        }else {
//            LOG.info("很遗憾，没抢到锁。");
//
//            throw new BusinessException(ResultCode.CONFIRM_ORDER_LOCK_FAIL.getCode(), ResultCode.CONFIRM_ORDER_LOCK_FAIL.getMessage());
//        }

        RLock lock = null;
         /*
            关于红锁（奇数台），看16.7节：
            A B C D E
            1: A B C D E
            2: C D E
            3: C
        */
        try {
            // 使用redisson 自带看门狗， 守护线程
            lock = redissonClient.getLock(lockKey);
            /**
             waitTime – the maximum time to acquire the lock 等待获取锁时间(最大尝试获得锁的时间)，超时返回false
             leaseTime – lease time 锁时长，即n秒后自动释放锁
             time unit – time unit 时间单位
             */
            // boolean tryLock = lock.tryLock(30, 10, TimeUnit.SECONDS); // 不带看门狗
            boolean tryLock = lock.tryLock(0, TimeUnit.SECONDS); // 带看门狗
             if (tryLock) {
                 LOG.info("恭喜，抢到锁了！");
                 // 可以把下面这段放开，只用一个线程来测试，看看redisson的看门狗效果
                 // for (int i = 0; i < 30; i++) {
                 //     Long expire = redisTemplate.opsForValue().getOperations().getExpire(lockKey);
                 //     LOG.info("锁过期时间还有：{}", expire);
                 //     Thread.sleep(1000);
                 // }
             } else {
                 // 只是没抢到锁，并不知道票抢完了没，所以提示稍候再试
                 LOG.info("很遗憾，没抢到锁");
                 throw new BusinessException(ResultCode.CONFIRM_ORDER_LOCK_FAIL.getCode(), ResultCode.CONFIRM_ORDER_LOCK_FAIL.getMessage());
             }

            // 省略 业务校验：车次是否存在，余票是否存在，车次是否在有效期内等

            // 数据保存到订单表
            DateTime now = DateTime.now();
            Date date = req.getDate();
            String trainCode = req.getTrainCode();
            String start = req.getStart();
            String end = req.getEnd();
            List<ConfirmOrderTicketReq> tickets = req.getTickets();

            ConfirmOrder confirmOrder = new ConfirmOrder();
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setMemberId(LoginMemberContext.getId());
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

            // 查询余票记录， 需要得到真实的库存
            DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(date, trainCode, start, end);
            LOG.info("查出余票记录：{}", dailyTrainTicket);

            // 预扣减 余票数量， 判断余票是否足够
            reduceTickets(req, dailyTrainTicket);

            // 最终的选座结果
            List<DailyTrainSeat> finalSeatList = new ArrayList<>();

            // 计算一个座位的偏移值
            // C1 D2=>[0,5]
            // A1, B1, C1=>[0,1,2]

            // 判断是否有选座
            ConfirmOrderTicketReq ticketReq0 = tickets.get(0);
            if (StrUtil.isNotBlank(ticketReq0.getSeat())) {
                LOG.info("本次购票有选座");
                List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(ticketReq0.getSeatTypeCode());// 一等座 or 二等座
                LOG.info("本次选座的座位类型包含的列：{}", colEnumList);

                List<String> referSeatList = new ArrayList<>();
                for (int i = 1; i <= 2; i++) {
                    for (SeatColEnum seatColEnum : colEnumList) {
                        referSeatList.add(seatColEnum.getCode()  + i);
                    }
                }
                LOG.info("用于作参照的两排座位：{}", referSeatList);

                List<Integer> offsetList = new ArrayList<>();
                // 绝对偏移值获取
                List<Integer> absoluteOffsetList = new ArrayList<>();
                for (ConfirmOrderTicketReq ticketReq : tickets) {
                    int index = referSeatList.indexOf(ticketReq.getSeat());
                    absoluteOffsetList.add(index);
                }
                LOG.info("计算得到所有座位的绝对偏移值：{}", absoluteOffsetList);

                for (Integer index : absoluteOffsetList) {
                    int offset = index - absoluteOffsetList.get(0);
                    offsetList.add(offset);
                }
                LOG.info("计算得到所有座位的相对第一个座位的偏移值：{}", offsetList);

                getSeat(finalSeatList, date, trainCode, ticketReq0.getSeatTypeCode(), ticketReq0.getSeat().split("")[0]
                        , offsetList, dailyTrainTicket.getStartIndex(), dailyTrainTicket.getEndIndex());

            }else {
                LOG.info("本次购票没有选座");

                for (ConfirmOrderTicketReq ticketReq : tickets) {
                    getSeat(finalSeatList, date, trainCode, ticketReq.getSeatTypeCode(), null
                            , null,
                            dailyTrainTicket.getStartIndex(), dailyTrainTicket.getEndIndex());
                }

            }

            LOG.info("最终的选座：{}", finalSeatList);

            // 选中座位后的处理
            try {
                afterConfirmOrderService.afterDoConfirm(dailyTrainTicket, finalSeatList, tickets, confirmOrder);

            }catch (Exception e) {
                LOG.error("保存购票信息失败", e);
                throw new BusinessException(ResultCode.CONFIRM_ORDER_EXCEPTION.getCode(), ResultCode.CONFIRM_ORDER_EXCEPTION.getMessage());

            }
//            // 删除分布式锁
//            LOG.info("购票流程结束，释放锁。");
//            redisTemplate.delete(lockKey);
        } catch (InterruptedException e) {
            LOG.info("购票异常：{}", e.getMessage());
        }finally {
            LOG.info("购票流程结束，释放锁。");
            if (null != lock && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }






    }

    /**
     * 挑座位， 如果有选座，则一次性挑完，如果无选座，则一个一个挑
     * @param date
     * @param trainCode
     * @param seatType
     * @param column
     * @param offsetList
     */
    private void getSeat(List<DailyTrainSeat> finalSeatList, Date date, String trainCode, String seatType, String column, List<Integer> offsetList, Integer startIndex, Integer endIndex) {
        List<DailyTrainSeat> getSeatList;
        List<DailyTrainCarriage> carriageList = dailyTrainCarriageService.selectBySeatType(date, trainCode, seatType);
        LOG.info("共查出 {} 个符合条件的车厢", carriageList.size());

        // 一个车厢一个车厢的获取座位数据
        for (DailyTrainCarriage carriage : carriageList) {
            LOG.info("开始从车厢 {} 选座", carriage.getIndex());
            getSeatList = new ArrayList<>();
            List<DailyTrainSeat> seatList = dailyTrainSeatService.selectByCarriageIndex(date, trainCode, carriage.getIndex());
            LOG.info("车厢 {} 的座位数：{}", carriage.getIndex(), seatList.size());

            for (int i = 0; i < seatList.size(); i++) {
                DailyTrainSeat dailyTrainSeat = seatList.get(i);
                String col = dailyTrainSeat.getCol();
                Integer seatIndex = dailyTrainSeat.getCarriageSeatIndex();

                // 判断当前座位不能被选中过
                boolean alreadyChooseFlag = false;
                for (DailyTrainSeat finalSeat : finalSeatList) {
                    if (finalSeat.getId().equals(dailyTrainSeat.getId())) {
                        alreadyChooseFlag = true;
                        break;
                    }
                }
                if (alreadyChooseFlag) {
                    LOG.info("座位{}被选中过，不能重复选中，继续判断下一个座位", seatIndex);
                    continue;
                }

                // 判断 column， 有值的话要比对序列
                if (StrUtil.isBlank(column)) {
                    LOG.info("无选座");
                } else {
                    if (!column.equals(col)) {
                        LOG.info("座位 {} 列值不对，继续判断下一个座位，当前列值：{}, 目标列值：{}", seatIndex, col, column);
                        continue;
                    }
                }


                boolean isChoose = calSell(dailyTrainSeat, startIndex, endIndex);
                if (isChoose) {
                    getSeatList.add(dailyTrainSeat);
                    LOG.info("选中座位");
                } else {
                    continue;
                }

                // 根据 offset 选座剩下的座位
                boolean isGetAllOffsetSeat = true;
                if (CollUtil.isNotEmpty(offsetList)) {
                    LOG.info("有偏移值：{},校验偏移的座位是否可选", offsetList);
                    // 从索引1开始， 索引0就是当前已选中的票
                    for (int j = 1; j < offsetList.size(); j++) {
                        Integer offset = offsetList.get(j);
                        // 座位的索引
//                        int nextIndex = seatIndex + offset - 1;
                        int nextIndex = i + offset;

                        // 有选座=》一定在同一车厢
                        if (nextIndex >= seatList.size()) {
                            LOG.info("座位{}不可选, 偏移后的索引超出列这个车厢的座位数", nextIndex);
                            isGetAllOffsetSeat = false;
                            break;

                        }

                        DailyTrainSeat nextDailyTrainSeat = seatList.get(nextIndex);
                        boolean isChooseNext = calSell(nextDailyTrainSeat, startIndex, endIndex);
                        if (isChooseNext) {
                            getSeatList.add(nextDailyTrainSeat);
                            LOG.info("座位{}被选中", nextDailyTrainSeat.getCarriageSeatIndex());
                        } else {
                            LOG.info("座位{}不可选", nextDailyTrainSeat.getCarriageSeatIndex());
                            isGetAllOffsetSeat = false;
                            break;
                        }
                    }
                }
                if (!isGetAllOffsetSeat) {
                    getSeatList = new ArrayList<>();
                    continue;
                }

                // 保存选好的座位
                finalSeatList.addAll(getSeatList);
                return;
            }
        }


    }

    /**
     * 计算某座位在区间内是否可卖
     * 例： sell = 10001 （0-1卖出去了，4-5卖出去了）
     */
    private boolean calSell(DailyTrainSeat dailyTrainSeat, Integer startIndex, Integer endIndex) {
        // 10001
        String sell = dailyTrainSeat.getSell();
        // 000
        String sellPart = sell.substring(startIndex, endIndex);
        if (Integer.parseInt(sellPart) > 0) {
            LOG.info("座位 {} 在本次车站区间 {}---{} 已售过票，不可选中该座位", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            return false;
        }else {
            LOG.info("座位 {} 在本次车站区间 {}---{} 未售过票，可选中该座位", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            // 111
            String curSell = sellPart.replace('0', '1');
            // 0111
            curSell = StrUtil.fillBefore(curSell, '0', endIndex);
            // 01110
            curSell = StrUtil.fillAfter(curSell, '0', sell.length());

            // curSell 与 sell 位运算=》卖出此票的售票详情
            int newSellInt = NumberUtil.binaryToInt(curSell) | NumberUtil.binaryToInt(sell);

            String newSell = NumberUtil.getBinaryStr(newSellInt);
            newSell = StrUtil.fillBefore(newSell, '0', sell.length());
            LOG.info("座位{}被选中，原售票信息：{}，车站区间：{}~{}，即：{}，最终售票信息：{}"
                    , dailyTrainSeat.getCarriageSeatIndex(), sell, startIndex, endIndex, curSell, newSell);

            dailyTrainSeat.setSell(newSell);

            return true;
        }
    }

    private static void reduceTickets(ConfirmOrderDoReq req, DailyTrainTicket dailyTrainTicket) {
        for (ConfirmOrderTicketReq reqTicket : req.getTickets()) {
            String seatTypeCode = reqTicket.getSeatTypeCode();
            SeatTypeEnum seatTypeEnum = EnumUtil.getBy(SeatTypeEnum::getCode, seatTypeCode);
            switch (seatTypeEnum) {
                case YDZ -> {
                    int countLeft = dailyTrainTicket.getYdz() - 1;
                    if (countLeft < 0) {
                        throw new BusinessException(ResultCode.CONFIRM_ORDER_TICKET_COUNT_ERROR.getCode(), ResultCode.CONFIRM_ORDER_TICKET_COUNT_ERROR.getMessage());
                    }
                    dailyTrainTicket.setYdz(countLeft);

                }
                case EDZ -> {
                    int countLeft = dailyTrainTicket.getEdz() - 1;
                    if (countLeft < 0) {
                        throw new BusinessException(ResultCode.CONFIRM_ORDER_TICKET_COUNT_ERROR.getCode(), ResultCode.CONFIRM_ORDER_TICKET_COUNT_ERROR.getMessage());
                    }
                    dailyTrainTicket.setEdz(countLeft);

                }
                case RW -> {
                    int countLeft = dailyTrainTicket.getRw() - 1;
                    if (countLeft < 0) {
                        throw new BusinessException(ResultCode.CONFIRM_ORDER_TICKET_COUNT_ERROR.getCode(), ResultCode.CONFIRM_ORDER_TICKET_COUNT_ERROR.getMessage());
                    }
                    dailyTrainTicket.setRw(countLeft);

                }
                case YW -> {
                    int countLeft = dailyTrainTicket.getYw() - 1;
                    if (countLeft < 0) {
                        throw new BusinessException(ResultCode.CONFIRM_ORDER_TICKET_COUNT_ERROR.getCode(), ResultCode.CONFIRM_ORDER_TICKET_COUNT_ERROR.getMessage());
                    }
                    dailyTrainTicket.setYw(countLeft);

                }
            }
        }
    }

    /**
     * 降级方法，需包含限流方法的所有参数和BlockException参数
     * @param req
     * @param e
     */
    public void doConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
        LOG.info("购票请求被限流：{}", req);
        throw new BusinessException(ResultCode.CONFIRM_ORDER_FLOW_EXCEPTION.getCode(), ResultCode.CONFIRM_ORDER_FLOW_EXCEPTION.getMessage());

    }
}
