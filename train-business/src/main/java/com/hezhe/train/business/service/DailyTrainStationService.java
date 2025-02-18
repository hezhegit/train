package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.util.SnowUtil;
import com.hezhe.train.business.domain.DailyTrainStation;
import com.hezhe.train.business.domain.DailyTrainStationExample;
import com.hezhe.train.business.mapper.DailyTrainStationMapper;
import com.hezhe.train.business.req.DailyTrainStationQueryReq;
import com.hezhe.train.business.req.DailyTrainStationSaveReq;
import com.hezhe.train.business.resp.DailyTrainStationQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyTrainStationService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainStationService.class);

    @Resource
    private DailyTrainStationMapper dailyTrainStationMapper;

    public void save(DailyTrainStationSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(req, DailyTrainStation.class);

        if (ObjectUtil.isNull(req.getId())) {
            // 新增
            dailyTrainStation.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.insert(dailyTrainStation);
        }else {
            dailyTrainStation.setUpdateTime(now);
            // 空值 也会更新
            dailyTrainStationMapper.updateByPrimaryKey(dailyTrainStation);
            // 空值 保留 原纪录值
//            dailyTrainStationMapper.updateByPrimaryKeySelective(dailyTrainStation);
        }



    }

    public PageResp<DailyTrainStationQueryResp> queryList(DailyTrainStationQueryReq req) {
        DailyTrainStationExample example = new DailyTrainStationExample();
        // id 倒序
        example.setOrderByClause("id desc");
        DailyTrainStationExample.Criteria criteria = example.createCriteria();
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrainStation> dailyTrainStations = dailyTrainStationMapper.selectByExample(example);
        // select count
        PageInfo<DailyTrainStation> pageInfo = new PageInfo<>(dailyTrainStations);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainStationQueryResp> dailyTrainStationQueryResps = BeanUtil.copyToList(dailyTrainStations, DailyTrainStationQueryResp.class);
        PageResp<DailyTrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(dailyTrainStationQueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        dailyTrainStationMapper.deleteByPrimaryKey(id);
    }
}
