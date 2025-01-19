package com.hezhe.train.member.req;

import com.hezhe.train.common.req.PageReq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassengerQueryReq extends PageReq {

    private Long memberId;

}