package com.hezhe.train.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessException extends RuntimeException{

    // "状态码"
    private Integer code;

    // "错误信息"
    private String errMsg;


    /**
     * 不写入堆栈信息 提高性能
     * @return
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }


}
