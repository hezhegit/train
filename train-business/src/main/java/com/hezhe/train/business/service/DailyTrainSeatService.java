package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.util.SnowUtil;
import com.hezhe.train.business.domain.DailyTrainSeat;
import com.hezhe.train.business.domain.DailyTrainSeatExample;
import com.hezhe.train.business.mapper.DailyTrainSeatMapper;
import com.hezhe.train.business.req.DailyTrainSeatQueryReq;
import com.hezhe.train.business.req.DailyTrainSeatSaveReq;
import com.hezhe.train.business.resp.DailyTrainSeatQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyTrainSeatService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainSeatService.class);

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    public void save(DailyTrainSeatSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(req, DailyTrainSeat.class);

        if (ObjectUtil.isNull(req.getId())) {
            // 新增
            dailyTrainSeat.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.insert(dailyTrainSeat);
        }else {
            dailyTrainSeat.setUpdateTime(now);
            // 空值 也会更新
            dailyTrainSeatMapper.updateByPrimaryKey(dailyTrainSeat);
            // 空值 保留 原纪录值
//            dailyTrainSeatMapper.updateByPrimaryKeySelective(dailyTrainSeat);
        }



    }

    public PageResp<DailyTrainSeatQueryResp> queryList(DailyTrainSeatQueryReq req) {
        DailyTrainSeatExample example = new DailyTrainSeatExample();
        // id 倒序
        example.setOrderByClause("id desc");
        DailyTrainSeatExample.Criteria criteria = example.createCriteria();
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrainSeat> dailyTrainSeats = dailyTrainSeatMapper.selectByExample(example);
        // select count
        PageInfo<DailyTrainSeat> pageInfo = new PageInfo<>(dailyTrainSeats);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainSeatQueryResp> dailyTrainSeatQueryResps = BeanUtil.copyToList(dailyTrainSeats, DailyTrainSeatQueryResp.class);
        PageResp<DailyTrainSeatQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(dailyTrainSeatQueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        dailyTrainSeatMapper.deleteByPrimaryKey(id);
    }
}
