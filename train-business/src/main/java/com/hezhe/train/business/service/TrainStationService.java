package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.business.domain.TrainStation;
import com.hezhe.train.business.domain.TrainStationExample;
import com.hezhe.train.business.mapper.TrainStationMapper;
import com.hezhe.train.business.req.TrainStationQueryReq;
import com.hezhe.train.business.req.TrainStationSaveReq;
import com.hezhe.train.business.resp.TrainStationQueryResp;
import com.hezhe.train.common.exception.BusinessException;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.ResultCode;
import com.hezhe.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainStationService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainStationService.class);

    @Resource
    private TrainStationMapper trainStationMapper;

    public void save(TrainStationSaveReq req) {
        DateTime now = DateTime.now();
        TrainStation trainStation = BeanUtil.copyProperties(req, TrainStation.class);

        if (ObjectUtil.isNull(req.getId())) {
            // 新增

            // 新增前 需要校验唯一健是否已经存在
            TrainStation trainStationDB = selectByUnique(trainStation.getTrainCode(),trainStation.getIndex());
            if (ObjectUtil.isNotEmpty(trainStationDB)) {
                throw new BusinessException(ResultCode.TRAIN_STATION_INDEX_ALREADY_EXIST.getCode(), ResultCode.TRAIN_STATION_INDEX_ALREADY_EXIST.getMessage());
            }

            // 新增前 需要校验唯一健是否已经存在
            trainStationDB = selectByUnique(trainStation.getTrainCode(),trainStation.getName());
            if (ObjectUtil.isNotEmpty(trainStationDB)) {
                throw new BusinessException(ResultCode.TRAIN_STATION_NAME_ALREADY_EXIST.getCode(), ResultCode.TRAIN_STATION_NAME_ALREADY_EXIST.getMessage());
            }


            trainStation.setId(SnowUtil.getSnowflakeNextId());
            trainStation.setCreateTime(now);
            trainStation.setUpdateTime(now);
            trainStationMapper.insert(trainStation);
        }else {
            trainStation.setUpdateTime(now);
            // 空值 也会更新
            trainStationMapper.updateByPrimaryKey(trainStation);
            // 空值 保留 原纪录值
//            trainStationMapper.updateByPrimaryKeySelective(trainStation);
        }



    }

    private TrainStation selectByUnique(String trainCode, Integer index) {
        TrainStationExample example = new TrainStationExample();
        TrainStationExample.Criteria criteria = example.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode)
                .andIndexEqualTo(index);
        List<TrainStation> list = trainStationMapper.selectByExample(example);
        if (CollUtil.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    private TrainStation selectByUnique(String trainCode, String name) {
        TrainStationExample example = new TrainStationExample();
        TrainStationExample.Criteria criteria = example.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode)
                .andNameEqualTo(name);
        List<TrainStation> list = trainStationMapper.selectByExample(example);
        if (CollUtil.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }


    public PageResp<TrainStationQueryResp> queryList(TrainStationQueryReq req) {
        TrainStationExample example = new TrainStationExample();
        example.setOrderByClause("train_code asc, `index` asc");
        TrainStationExample.Criteria criteria = example.createCriteria();

        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<TrainStation> trainStations = trainStationMapper.selectByExample(example);
        // select count
        PageInfo<TrainStation> pageInfo = new PageInfo<>(trainStations);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TrainStationQueryResp> trainStationQueryResps = BeanUtil.copyToList(trainStations, TrainStationQueryResp.class);
        PageResp<TrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(trainStationQueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        trainStationMapper.deleteByPrimaryKey(id);
    }

    public List<TrainStation> selectByTrainCode(String trainCode) {
        TrainStationExample example = new TrainStationExample();
        example.setOrderByClause("`index` asc");
        TrainStationExample.Criteria criteria = example.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode);
        return trainStationMapper.selectByExample(example);
    }

}
