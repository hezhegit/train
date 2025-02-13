package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.util.SnowUtil;
import com.hezhe.train.business.domain.Station;
import com.hezhe.train.business.domain.StationExample;
import com.hezhe.train.business.mapper.StationMapper;
import com.hezhe.train.business.req.StationQueryReq;
import com.hezhe.train.business.req.StationSaveReq;
import com.hezhe.train.business.resp.StationQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {

    private static final Logger LOG = LoggerFactory.getLogger(StationService.class);

    @Resource
    private StationMapper stationMapper;

    public void save(StationSaveReq req) {
        DateTime now = DateTime.now();
        Station station = BeanUtil.copyProperties(req, Station.class);

        if (ObjectUtil.isNull(req.getId())) {
            // 新增
            station.setId(SnowUtil.getSnowflakeNextId());
            station.setCreateTime(now);
            station.setUpdateTime(now);
            stationMapper.insert(station);
        }else {
            station.setUpdateTime(now);
            // 空值 也会更新
            stationMapper.updateByPrimaryKey(station);
            // 空值 保留 原纪录值
//            stationMapper.updateByPrimaryKeySelective(station);
        }



    }

    public PageResp<StationQueryResp> queryList(StationQueryReq req) {
        StationExample example = new StationExample();
        // id 倒序
        example.setOrderByClause("id desc");
        StationExample.Criteria criteria = example.createCriteria();
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Station> stations = stationMapper.selectByExample(example);
        // select count
        PageInfo<Station> pageInfo = new PageInfo<>(stations);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<StationQueryResp> stationQueryResps = BeanUtil.copyToList(stations, StationQueryResp.class);
        PageResp<StationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(stationQueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        stationMapper.deleteByPrimaryKey(id);
    }
}
