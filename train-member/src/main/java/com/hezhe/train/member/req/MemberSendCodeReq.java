package com.hezhe.train.member.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberSendCodeReq {
    @NotBlank(message = "【手机号】不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
    private String mobile;
}
