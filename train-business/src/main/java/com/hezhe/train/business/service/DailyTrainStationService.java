package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.business.domain.DailyTrainStation;
import com.hezhe.train.business.domain.DailyTrainStationExample;
import com.hezhe.train.business.domain.TrainStation;
import com.hezhe.train.business.mapper.DailyTrainStationMapper;
import com.hezhe.train.business.req.DailyTrainStationQueryReq;
import com.hezhe.train.business.req.DailyTrainStationSaveReq;
import com.hezhe.train.business.resp.DailyTrainStationQueryResp;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainStationService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainStationService.class);

    @Resource
    private DailyTrainStationMapper dailyTrainStationMapper;

    @Resource
    private TrainStationService trainStationService;

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
        example.setOrderByClause("date desc, train_code asc, `index` asc");
        DailyTrainStationExample.Criteria criteria = example.createCriteria();

        if (ObjectUtil.isNotNull(req.getDate())) {
            criteria.andDateEqualTo(req.getDate());
        }

        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }


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

    @Transactional
    public void genDaily(Date date, String trainCode) {
        LOG.info("生成日期：【{}】 === 车次：【{}】 的车站信息==开始==", DateUtil.formatDate(date), trainCode);
        // 删除某日某车次的车站信息
        DailyTrainStationExample example = new DailyTrainStationExample();
        example.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);
        dailyTrainStationMapper.deleteByExample(example);

        // 查出某车次所有的车站信息
        List<TrainStation> trainStations = trainStationService.selectByTrainCode(trainCode);
        if (CollUtil.isEmpty(trainStations)) {
            LOG.info("该车次没有车站基础数据，生成该车次的车站信息结束！");
            return;
        }
        for (TrainStation trainStation : trainStations) {
            DateTime now = DateTime.now();
            DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(trainStation, DailyTrainStation.class);
            dailyTrainStation.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStation.setDate(date);
            dailyTrainStationMapper.insert(dailyTrainStation);
        }

        LOG.info("开始生成日期：【{}】 === 车次：【{}】 的车站信息==结束==", DateUtil.formatDate(date), trainCode);


    }
}
