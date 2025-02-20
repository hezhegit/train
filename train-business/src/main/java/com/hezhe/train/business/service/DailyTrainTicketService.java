package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.util.SnowUtil;
import com.hezhe.train.business.domain.DailyTrainTicket;
import com.hezhe.train.business.domain.DailyTrainTicketExample;
import com.hezhe.train.business.mapper.DailyTrainTicketMapper;
import com.hezhe.train.business.req.DailyTrainTicketQueryReq;
import com.hezhe.train.business.req.DailyTrainTicketSaveReq;
import com.hezhe.train.business.resp.DailyTrainTicketQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyTrainTicketService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainTicketService.class);

    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    public void save(DailyTrainTicketSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainTicket dailyTrainTicket = BeanUtil.copyProperties(req, DailyTrainTicket.class);

        if (ObjectUtil.isNull(req.getId())) {
            // 新增
            dailyTrainTicket.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainTicket.setCreateTime(now);
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.insert(dailyTrainTicket);
        }else {
            dailyTrainTicket.setUpdateTime(now);
            // 空值 也会更新
            dailyTrainTicketMapper.updateByPrimaryKey(dailyTrainTicket);
            // 空值 保留 原纪录值
//            dailyTrainTicketMapper.updateByPrimaryKeySelective(dailyTrainTicket);
        }



    }

    public PageResp<DailyTrainTicketQueryResp> queryList(DailyTrainTicketQueryReq req) {
        DailyTrainTicketExample example = new DailyTrainTicketExample();
        // id 倒序
        example.setOrderByClause("id desc");
        DailyTrainTicketExample.Criteria criteria = example.createCriteria();
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrainTicket> dailyTrainTickets = dailyTrainTicketMapper.selectByExample(example);
        // select count
        PageInfo<DailyTrainTicket> pageInfo = new PageInfo<>(dailyTrainTickets);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainTicketQueryResp> dailyTrainTicketQueryResps = BeanUtil.copyToList(dailyTrainTickets, DailyTrainTicketQueryResp.class);
        PageResp<DailyTrainTicketQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(dailyTrainTicketQueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        dailyTrainTicketMapper.deleteByPrimaryKey(id);
    }
}
