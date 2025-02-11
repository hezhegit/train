package com.hezhe.train.${module}.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
<#--import com.hezhe.train.common.context.LoginMemberContext;-->
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.util.SnowUtil;
import com.hezhe.train.${module}.domain.${Domain};
import com.hezhe.train.${module}.domain.${Domain}Example;
import com.hezhe.train.${module}.mapper.${Domain}Mapper;
import com.hezhe.train.${module}.req.${Domain}QueryReq;
import com.hezhe.train.${module}.req.${Domain}SaveReq;
import com.hezhe.train.${module}.resp.${Domain}QueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ${Domain}Service {

    private static final Logger LOG = LoggerFactory.getLogger(${Domain}Service.class);

    @Resource
    private ${Domain}Mapper ${domain}Mapper;

    public void save(${Domain}SaveReq req) {
        DateTime now = DateTime.now();
        ${Domain} ${domain} = BeanUtil.copyProperties(req, ${Domain}.class);

        if (ObjectUtil.isNull(req.getId())) {
            // 新增
<#--            ${domain}.setMemberId(LoginMemberContext.getId());-->
            ${domain}.setId(SnowUtil.getSnowflakeNextId());
            ${domain}.setCreateTime(now);
            ${domain}.setUpdateTime(now);
            ${domain}Mapper.insert(${domain});
        }else {
            ${domain}.setUpdateTime(now);
            // 空值 也会更新
            ${domain}Mapper.updateByPrimaryKey(${domain});
            // 空值 保留 原纪录值
//            ${domain}Mapper.updateByPrimaryKeySelective(${domain});
        }



    }

    public PageResp<${Domain}QueryResp> queryList(${Domain}QueryReq req) {
        ${Domain}Example example = new ${Domain}Example();
        // id 倒序
        example.setOrderByClause("id desc");
        ${Domain}Example.Criteria criteria = example.createCriteria();
<#--        if (ObjectUtil.isNotNull(req.getMemberId())) {-->
<#--            criteria.andMemberIdEqualTo(req.getMemberId());-->
<#--        }-->
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<${Domain}> ${domain}s = ${domain}Mapper.selectByExample(example);
        // select count
        PageInfo<${Domain}> pageInfo = new PageInfo<>(${domain}s);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<${Domain}QueryResp> ${domain}QueryResps = BeanUtil.copyToList(${domain}s, ${Domain}QueryResp.class);
        PageResp<${Domain}QueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(${domain}QueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        ${domain}Mapper.deleteByPrimaryKey(id);
    }
}
