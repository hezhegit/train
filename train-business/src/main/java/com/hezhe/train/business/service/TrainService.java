package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.business.domain.Train;
import com.hezhe.train.business.domain.TrainExample;
import com.hezhe.train.business.mapper.TrainMapper;
import com.hezhe.train.business.req.TrainQueryReq;
import com.hezhe.train.business.req.TrainSaveReq;
import com.hezhe.train.business.resp.TrainQueryResp;
import com.hezhe.train.common.exception.BusinessException;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.resp.ResultCode;
import com.hezhe.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainService.class);

    @Resource
    private TrainMapper trainMapper;

    public void save(TrainSaveReq req) {
        DateTime now = DateTime.now();
        Train train = BeanUtil.copyProperties(req, Train.class);

        if (ObjectUtil.isNull(req.getId())) {
            // 新增

            // 新增前 需要校验唯一健是否已经存在
            Train trainDB = selectByUnique(train.getCode());

            if (ObjectUtil.isNotEmpty(trainDB)) {
                throw new BusinessException(ResultCode.TRAIN_ALREADY_EXIST.getCode(), ResultCode.TRAIN_ALREADY_EXIST.getMessage());
            }

            train.setId(SnowUtil.getSnowflakeNextId());
            train.setCreateTime(now);
            train.setUpdateTime(now);
            trainMapper.insert(train);
        }else {
            train.setUpdateTime(now);
            // 空值 也会更新
            trainMapper.updateByPrimaryKey(train);
            // 空值 保留 原纪录值
//            trainMapper.updateByPrimaryKeySelective(train);
        }



    }

    private Train selectByUnique(String code) {
        TrainExample example = new TrainExample();
        TrainExample.Criteria criteria = example.createCriteria();
        criteria.andCodeEqualTo(code);
        List<Train> list = trainMapper.selectByExample(example);
        if (CollUtil.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    public PageResp<TrainQueryResp> queryList(TrainQueryReq req) {
        TrainExample example = new TrainExample();
        // id 倒序
        example.setOrderByClause("code asc");
        TrainExample.Criteria criteria = example.createCriteria();
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Train> trains = trainMapper.selectByExample(example);
        // select count
        PageInfo<Train> pageInfo = new PageInfo<>(trains);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TrainQueryResp> trainQueryResps = BeanUtil.copyToList(trains, TrainQueryResp.class);
        PageResp<TrainQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(trainQueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        trainMapper.deleteByPrimaryKey(id);
    }

    @Transactional
    public List<TrainQueryResp> queryAll() {
        List<Train> trainList = selectAll();
//        LOG.info("=====再查一次=====");
//        trainList = selectAll();
        return BeanUtil.copyToList(trainList, TrainQueryResp.class);
    }

    public List<Train> selectAll() {
        TrainExample example = new TrainExample();
        example.setOrderByClause("code asc");
        return trainMapper.selectByExample(example);
    }

}
