package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.business.domain.DailyTrain;
import com.hezhe.train.business.domain.DailyTrainExample;
import com.hezhe.train.business.domain.Train;
import com.hezhe.train.business.mapper.DailyTrainMapper;
import com.hezhe.train.business.req.DailyTrainQueryReq;
import com.hezhe.train.business.req.DailyTrainSaveReq;
import com.hezhe.train.business.resp.DailyTrainQueryResp;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainService.class);

    @Resource
    private DailyTrainMapper dailyTrainMapper;

    @Resource
    private TrainService trainService;

    @Resource
    private DailyTrainStationService dailyTrainStationService;

    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;

    public void save(DailyTrainSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrain dailyTrain = BeanUtil.copyProperties(req, DailyTrain.class);

        if (ObjectUtil.isNull(req.getId())) {
            // 新增
            dailyTrain.setId(SnowUtil.getSnowflakeNextId());
            dailyTrain.setCreateTime(now);
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.insert(dailyTrain);
        }else {
            dailyTrain.setUpdateTime(now);
            // 空值 也会更新
            dailyTrainMapper.updateByPrimaryKey(dailyTrain);
            // 空值 保留 原纪录值
//            dailyTrainMapper.updateByPrimaryKeySelective(dailyTrain);
        }



    }

    public PageResp<DailyTrainQueryResp> queryList(DailyTrainQueryReq req) {
        DailyTrainExample example = new DailyTrainExample();
        // id 倒序
        example.setOrderByClause("date desc, code asc");
        DailyTrainExample.Criteria criteria = example.createCriteria();

        if (ObjectUtil.isNotNull(req.getDate())) {
            criteria.andDateEqualTo(req.getDate());
        }

        if (ObjectUtil.isNotEmpty(req.getCode())) {
            criteria.andCodeEqualTo(req.getCode());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrain> dailyTrains = dailyTrainMapper.selectByExample(example);
        // select count
        PageInfo<DailyTrain> pageInfo = new PageInfo<>(dailyTrains);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainQueryResp> dailyTrainQueryResps = BeanUtil.copyToList(dailyTrains, DailyTrainQueryResp.class);
        PageResp<DailyTrainQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(dailyTrainQueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        dailyTrainMapper.deleteByPrimaryKey(id);
    }

    /**
     * 生成某人所有车次信息 包括 车次 车站 车厢 座位
     * @param date
     */
    public void generateDaily(Date date) {
        List<Train> trainList = trainService.selectAll();
        if (CollUtil.isEmpty(trainList)) {
            LOG.info("没有车次基础数据，任务结束！");
            return;
        }

        for (Train train : trainList) {
            generateDailyTrain(date, train);
        }
    }

    public void generateDailyTrain(Date date, Train train) {
        LOG.info("生成日期：【{}】 === 车次：【{}】的信息==开始==", DateUtil.formatDate(date), train.getCode());
        // 删除该车次已有的数据
        DailyTrainExample example = new DailyTrainExample();
        example.createCriteria()
                .andDateEqualTo(date)
                .andCodeEqualTo(train.getCode());

        dailyTrainMapper.deleteByExample(example);

        // 生成该车次的数据
        DateTime now = DateTime.now();
        DailyTrain dailyTrain = BeanUtil.copyProperties(train, DailyTrain.class);
        dailyTrain.setId(SnowUtil.getSnowflakeNextId());
        dailyTrain.setCreateTime(now);
        dailyTrain.setUpdateTime(now);
        dailyTrain.setDate(date);
        dailyTrainMapper.insert(dailyTrain);

        // 生成该该车次的车站数据
        dailyTrainStationService.genDaily(date, train.getCode());

        // 生成该该车次的车厢数据
        dailyTrainCarriageService.genDaily(date, train.getCode());

        LOG.info("生成日期：【{}】 === 车次：【{}】的信息==结束==", DateUtil.formatDate(date), train.getCode());

    }
}
