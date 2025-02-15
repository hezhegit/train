package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.business.domain.TrainCarriage;
import com.hezhe.train.business.domain.TrainCarriageExample;
import com.hezhe.train.business.enums.SeatColEnum;
import com.hezhe.train.business.mapper.TrainCarriageMapper;
import com.hezhe.train.business.req.TrainCarriageQueryReq;
import com.hezhe.train.business.req.TrainCarriageSaveReq;
import com.hezhe.train.business.resp.TrainCarriageQueryResp;
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
public class TrainCarriageService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainCarriageService.class);

    @Resource
    private TrainCarriageMapper trainCarriageMapper;

    public void save(TrainCarriageSaveReq req) {
        DateTime now = DateTime.now();

        // 自动计算出列数和总座位数
        List<SeatColEnum> seatColEnums = SeatColEnum.getColsByType(req.getSeatType());
        req.setColCount(seatColEnums.size());
        req.setSeatCount(req.getColCount() * req.getRowCount());

        TrainCarriage trainCarriage = BeanUtil.copyProperties(req, TrainCarriage.class);

        if (ObjectUtil.isNull(req.getId())) {
            // 新增

            // 新增前 需要校验唯一健是否已经存在
            TrainCarriage trainCarriageDB = selectByUnique(trainCarriage.getTrainCode(), trainCarriage.getIndex());

            if (ObjectUtil.isNotEmpty(trainCarriageDB)) {
                throw new BusinessException(ResultCode.TRAIN_CARRIAGE_INDEX_ALREADY_EXIST.getCode(), ResultCode.TRAIN_CARRIAGE_INDEX_ALREADY_EXIST.getMessage());
            }


            trainCarriage.setId(SnowUtil.getSnowflakeNextId());
            trainCarriage.setCreateTime(now);
            trainCarriage.setUpdateTime(now);
            trainCarriageMapper.insert(trainCarriage);
        }else {
            trainCarriage.setUpdateTime(now);
            // 空值 也会更新
            trainCarriageMapper.updateByPrimaryKey(trainCarriage);
            // 空值 保留 原纪录值
//            trainCarriageMapper.updateByPrimaryKeySelective(trainCarriage);
        }

    }

    private TrainCarriage selectByUnique(String trainCode, Integer index) {
        TrainCarriageExample example = new TrainCarriageExample();
        TrainCarriageExample.Criteria criteria = example.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode)
                .andIndexEqualTo(index);
        List<TrainCarriage> list = trainCarriageMapper.selectByExample(example);
        if (CollUtil.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    public PageResp<TrainCarriageQueryResp> queryList(TrainCarriageQueryReq req) {
        TrainCarriageExample example = new TrainCarriageExample();
        // id 倒序
        example.setOrderByClause("train_code asc, `index` asc");
        TrainCarriageExample.Criteria criteria = example.createCriteria();

        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }


        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<TrainCarriage> trainCarriages = trainCarriageMapper.selectByExample(example);
        // select count
        PageInfo<TrainCarriage> pageInfo = new PageInfo<>(trainCarriages);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TrainCarriageQueryResp> trainCarriageQueryResps = BeanUtil.copyToList(trainCarriages, TrainCarriageQueryResp.class);
        PageResp<TrainCarriageQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(trainCarriageQueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        trainCarriageMapper.deleteByPrimaryKey(id);
    }

    public List<TrainCarriage> selectByTrainCode(String trainCode) {
        TrainCarriageExample example = new TrainCarriageExample();
        example.setOrderByClause("`index` asc");
        example.createCriteria().andTrainCodeEqualTo(trainCode);
        return trainCarriageMapper.selectByExample(example);
    }
}
