package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.util.SnowUtil;
import com.hezhe.train.business.domain.SkToken;
import com.hezhe.train.business.domain.SkTokenExample;
import com.hezhe.train.business.mapper.SkTokenMapper;
import com.hezhe.train.business.req.SkTokenQueryReq;
import com.hezhe.train.business.req.SkTokenSaveReq;
import com.hezhe.train.business.resp.SkTokenQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(SkTokenService.class);

    @Resource
    private SkTokenMapper skTokenMapper;

    public void save(SkTokenSaveReq req) {
        DateTime now = DateTime.now();
        SkToken skToken = BeanUtil.copyProperties(req, SkToken.class);

        if (ObjectUtil.isNull(req.getId())) {
            // 新增
            skToken.setId(SnowUtil.getSnowflakeNextId());
            skToken.setCreateTime(now);
            skToken.setUpdateTime(now);
            skTokenMapper.insert(skToken);
        }else {
            skToken.setUpdateTime(now);
            // 空值 也会更新
            skTokenMapper.updateByPrimaryKey(skToken);
            // 空值 保留 原纪录值
//            skTokenMapper.updateByPrimaryKeySelective(skToken);
        }



    }

    public PageResp<SkTokenQueryResp> queryList(SkTokenQueryReq req) {
        SkTokenExample example = new SkTokenExample();
        // id 倒序
        example.setOrderByClause("id desc");
        SkTokenExample.Criteria criteria = example.createCriteria();
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<SkToken> skTokens = skTokenMapper.selectByExample(example);
        // select count
        PageInfo<SkToken> pageInfo = new PageInfo<>(skTokens);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<SkTokenQueryResp> skTokenQueryResps = BeanUtil.copyToList(skTokens, SkTokenQueryResp.class);
        PageResp<SkTokenQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(skTokenQueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        skTokenMapper.deleteByPrimaryKey(id);
    }
}
