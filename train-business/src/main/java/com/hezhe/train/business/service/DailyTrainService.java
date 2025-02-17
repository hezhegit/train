package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.util.SnowUtil;
import com.hezhe.train.business.domain.DailyTrain;
import com.hezhe.train.business.domain.DailyTrainExample;
import com.hezhe.train.business.mapper.DailyTrainMapper;
import com.hezhe.train.business.req.DailyTrainQueryReq;
import com.hezhe.train.business.req.DailyTrainSaveReq;
import com.hezhe.train.business.resp.DailyTrainQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyTrainService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainService.class);

    @Resource
    private DailyTrainMapper dailyTrainMapper;

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
}
