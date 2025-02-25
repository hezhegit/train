package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.business.domain.*;
import com.hezhe.train.business.enums.ConfirmOrderStatusEnum;
import com.hezhe.train.business.enums.SeatColEnum;
import com.hezhe.train.business.enums.SeatTypeEnum;
import com.hezhe.train.business.req.ConfirmOrderTicketReq;
import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.exception.BusinessException;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.ResultCode;
import com.hezhe.train.common.util.SnowUtil;
import com.hezhe.train.business.mapper.ConfirmOrderMapper;
import com.hezhe.train.business.req.ConfirmOrderQueryReq;
import com.hezhe.train.business.req.ConfirmOrderDoReq;
import com.hezhe.train.business.resp.ConfirmOrderQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public void doConfirm(ConfirmOrderDoReq req) {
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

            getSeat(date, trainCode, ticketReq0.getSeatTypeCode(), ticketReq0.getSeat().split("")[0]
            , offsetList);

        }else {
            LOG.info("本次购票没有选座");

            for (ConfirmOrderTicketReq ticketReq : tickets) {
                getSeat(date, trainCode, ticketReq.getSeatTypeCode(), null
                        , null);
            }

        }


    }

    private void getSeat(Date date, String trainCode, String seatType, String column, List<Integer> offsetList) {
        List<DailyTrainCarriage> carriageList = dailyTrainCarriageService.selectBySeatType(date, trainCode, seatType);
        LOG.info("共查出 {} 个符合条件的车厢", carriageList.size());

        // 一个车厢一个车厢的获取座位数据
        carriageList.forEach(carriage -> {
            LOG.info("开始从车厢 {} 选座", carriage.getIndex());
            List<DailyTrainSeat> seatList = dailyTrainSeatService.selectByCarriageIndex(date, trainCode, carriage.getIndex());
            LOG.info("车厢 {} 的座位数：{}", carriage.getIndex(), seatList.size());
        });


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
}
