package com.hezhe.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.common.context.LoginMemberContext;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.util.SnowUtil;
import com.hezhe.train.member.domain.Passenger;
import com.hezhe.train.member.domain.PassengerExample;
import com.hezhe.train.member.mapper.PassengerMapper;
import com.hezhe.train.member.req.PassengerQueryReq;
import com.hezhe.train.member.req.PassengerSaveReq;
import com.hezhe.train.member.resp.PassengerQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerService {

    private static final Logger LOG = LoggerFactory.getLogger(PassengerService.class);

    @Resource
    private PassengerMapper passengerMapper;

    public void save(PassengerSaveReq req) {
        DateTime now = DateTime.now();
        Passenger passenger = BeanUtil.copyProperties(req, Passenger.class);

        if (ObjectUtil.isNull(req.getId())) {
            // 新增
            passenger.setMemberId(LoginMemberContext.getId());
            passenger.setId(SnowUtil.getSnowflakeNextId());
            passenger.setCreateTime(now);
            passenger.setUpdateTime(now);
            passengerMapper.insert(passenger);
        }else {
            passenger.setUpdateTime(now);
            // 空值 也会更新
            passengerMapper.updateByPrimaryKey(passenger);
            // 空值 保留 原纪录值
//            passengerMapper.updateByPrimaryKeySelective(passenger);
        }



    }

    public PageResp<PassengerQueryResp> queryList(PassengerQueryReq req) {
        PassengerExample example = new PassengerExample();
        // id 倒序
        example.setOrderByClause("id desc");
        PassengerExample.Criteria criteria = example.createCriteria();
        if (ObjectUtil.isNotNull(req.getMemberId())) {
            criteria.andMemberIdEqualTo(req.getMemberId());
        }
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Passenger> passengers = passengerMapper.selectByExample(example);
        // select count
        PageInfo<Passenger> pageInfo = new PageInfo<>(passengers);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<PassengerQueryResp> passengerQueryResps = BeanUtil.copyToList(passengers, PassengerQueryResp.class);
        PageResp<PassengerQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(passengerQueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        passengerMapper.deleteByPrimaryKey(id);
    }


    /* *
    * 查找我的所有乘客
    * */
    public List<PassengerQueryResp> queryMine() {
        PassengerExample example = new PassengerExample();
        example.setOrderByClause("name asc");
        PassengerExample.Criteria criteria = example.createCriteria();
        criteria.andMemberIdEqualTo(LoginMemberContext.getId());
        List<Passenger> passengers = passengerMapper.selectByExample(example);
        return BeanUtil.copyToList(passengers, PassengerQueryResp.class);
    }
}
