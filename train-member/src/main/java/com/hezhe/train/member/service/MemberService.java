package com.hezhe.train.member.service;

import com.hezhe.train.member.domain.Member;
import com.hezhe.train.member.domain.MemberExample;
import com.hezhe.train.member.mapper.MemberMapper;
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

    public long register(String mobile) {

        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> members = memberMapper.selectByExample(memberExample);

        if (!members.isEmpty()) {
            throw new RuntimeException("手机号已注册！");
        }

        Member member = new Member();
        member.setMobile(mobile);
        member.setId(System.currentTimeMillis());
        memberMapper.insert(member);
        return member.getId();
    }
}
