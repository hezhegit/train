package com.hezhe.train.member.service;

import com.hezhe.train.common.exception.BusinessException;
import com.hezhe.train.common.resp.ResultCode;
import com.hezhe.train.common.util.SnowUtil;
import com.hezhe.train.member.domain.Member;
import com.hezhe.train.member.domain.MemberExample;
import com.hezhe.train.member.mapper.MemberMapper;
import com.hezhe.train.member.req.MemberRegisterReq;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    @Resource
    private MemberMapper memberMapper;

    public int count(){
        return Math.toIntExact(memberMapper.countByExample(null));
    }

    public long register(MemberRegisterReq req) {

        String mobile = req.getMobile();


        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> members = memberMapper.selectByExample(memberExample);

        if (!members.isEmpty()) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_ALREADY_EXIST.getCode(), ResultCode.USER_ACCOUNT_ALREADY_EXIST.getMessage());
        }

        Member member = new Member();
        member.setMobile(mobile);
        member.setId(SnowUtil.getSnowflakeNextId());
        memberMapper.insert(member);
        return member.getId();
    }
}
