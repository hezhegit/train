package com.hezhe.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.hezhe.train.common.exception.BusinessException;
import com.hezhe.train.common.resp.ResultCode;
import com.hezhe.train.common.util.JwtUtil;
import com.hezhe.train.common.util.SnowUtil;
import com.hezhe.train.member.domain.Member;
import com.hezhe.train.member.domain.MemberExample;
import com.hezhe.train.member.mapper.MemberMapper;
import com.hezhe.train.member.req.MemberLoginReq;
import com.hezhe.train.member.req.MemberRegisterReq;
import com.hezhe.train.member.req.MemberSendCodeReq;
import com.hezhe.train.member.resp.MemberLoginResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private static final Logger LOG = LoggerFactory.getLogger(MemberService.class);

    @Resource
    private MemberMapper memberMapper;

    public int count(){
        return Math.toIntExact(memberMapper.countByExample(null));
    }

    public long register(MemberRegisterReq req) {

        String mobile = req.getMobile();


        Member memberDB = selectByMobile(mobile);

        if (!ObjectUtil.isNull(memberDB)) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_ALREADY_EXIST.getCode(), ResultCode.USER_ACCOUNT_ALREADY_EXIST.getMessage());
        }

        Member member = new Member();
        member.setMobile(mobile);
        member.setId(SnowUtil.getSnowflakeNextId());
        memberMapper.insert(member);
        return member.getId();
    }

    public void sendCode(MemberSendCodeReq req) {

        String mobile = req.getMobile();


        Member memberDB = selectByMobile(mobile);

        // 手机号不存在=》insert
        if (ObjectUtil.isNull(memberDB)) {
            LOG.info("手机号不存在,插入一条数据。");
            Member member = new Member();
            member.setMobile(mobile);
            member.setId(SnowUtil.getSnowflakeNextId());
            memberMapper.insert(member);
//            throw new BusinessException(ResultCode.USER_ACCOUNT_ALREADY_EXIST.getCode(), ResultCode.USER_ACCOUNT_ALREADY_EXIST.getMessage());
        }else {
            LOG.info("手机号存在,直接登录。");
        }

        // 生成验证码
        String code = RandomUtil.randomString(4);
        LOG.info("生成短信验证码：{}", code);

        // 短信操作
    }

    public MemberLoginResp login(MemberLoginReq req) {

        String mobile = req.getMobile();
        String code = req.getCode();


        Member memberDB = selectByMobile(mobile);

        // 手机号不存在
        if (ObjectUtil.isNull(memberDB)) {
            throw new BusinessException(ResultCode.USER_MOBILE_NOT_EXIST.getCode(), ResultCode.USER_MOBILE_NOT_EXIST.getMessage());
        }

        // 短信验证校验
        if (!"8888".equals(code)) {
            throw new BusinessException(ResultCode.USER_MOBILE_CODE_ERROR.getCode(), ResultCode.USER_MOBILE_CODE_ERROR.getMessage());
        }

        MemberLoginResp memberLoginResp = BeanUtil.copyProperties(memberDB, MemberLoginResp.class);
        // JWT：Header.Payload.Signature
        // Header：包含令牌的类型和签名算法的设置
        // Payload（载荷信息）：存放实际传输的数据，比如用户信息
        // Signature（签名）：签名由上面两部分+密钥组成

//        // 载荷信息
//        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(memberLoginResp);
//        // 密钥
//        String key = "ticket12306";
//        String token = JWTUtil.createToken(stringObjectMap, key.getBytes());

        String token = JwtUtil.createToken(memberLoginResp.getId(), memberLoginResp.getMobile());

        memberLoginResp.setToken(token);

        return memberLoginResp;

    }

    private Member selectByMobile(String mobile) {
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> members = memberMapper.selectByExample(memberExample);
        if (CollUtil.isEmpty(members)) {
            return null;
        }else {
            return members.get(0);
        }
    }
}
