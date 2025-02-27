package com.hezhe.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.common.req.MemberTicketReq;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.util.SnowUtil;
import com.hezhe.train.member.domain.Ticket;
import com.hezhe.train.member.domain.TicketExample;
import com.hezhe.train.member.mapper.TicketMapper;
import com.hezhe.train.member.req.TicketQueryReq;
import com.hezhe.train.member.resp.TicketQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private static final Logger LOG = LoggerFactory.getLogger(TicketService.class);

    @Resource
    private TicketMapper ticketMapper;

    public void save(MemberTicketReq req) throws Exception {
        // LOG.info("seata全局事务ID save: {}", RootContext.getXID());
        DateTime now = DateTime.now();
        Ticket ticket = BeanUtil.copyProperties(req, Ticket.class);
        ticket.setId(SnowUtil.getSnowflakeNextId());
        ticket.setCreateTime(now);
        ticket.setUpdateTime(now);
        ticketMapper.insert(ticket);
        // 模拟被调用方出现异常
        // if (1 == 1) {
        //     throw new Exception("测试异常11");
        // }
    }

    public PageResp<TicketQueryResp> queryList(TicketQueryReq req) {
        TicketExample example = new TicketExample();
        // id 倒序
        example.setOrderByClause("id desc");
        TicketExample.Criteria criteria = example.createCriteria();
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Ticket> tickets = ticketMapper.selectByExample(example);
        // select count
        PageInfo<Ticket> pageInfo = new PageInfo<>(tickets);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TicketQueryResp> ticketQueryResps = BeanUtil.copyToList(tickets, TicketQueryResp.class);
        PageResp<TicketQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(ticketQueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        ticketMapper.deleteByPrimaryKey(id);
    }
}
