package com.hezhe.train.common.controller;

import cn.hutool.core.util.StrUtil;
import com.hezhe.train.common.exception.BusinessException;
import com.hezhe.train.common.resp.Result;
import com.hezhe.train.common.resp.ResultCode;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一异常处理、数据预处理等
 */
@ControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    /**
     * 全局异常处理,没有指定异常的类型,不管什么异常都是可以捕获的
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e) throws Exception {
        //e.printStackTrace();
         LOG.info("seata全局事务ID==: {}", RootContext.getXID());
         // 如果是在一次全局事务里出异常了，就不要包装返回值，将异常抛给调用方，让调用方回滚事务
         if (StrUtil.isNotBlank(RootContext.getXID())) {
             throw e;
         }
        LOG.error(e.getMessage());
        return Result.error().message("系统出现异常，请联系管理员1");
    }

    /**
     * 处理特定异常类型,可以定义多个,这里只以ArithmeticException为例
     * @param e
     * @return
     */
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public Result error(ArithmeticException e){
        //e.printStackTrace();
        LOG.error(e.getMessage());
        return Result.error().code(ResultCode.ARITHMETIC_EXCEPTION.getCode())
                .message(ResultCode.ARITHMETIC_EXCEPTION.getMessage());
    }


    /**
     * 处理特定异常类型,可以定义多个,这里只以ArithmeticException为例
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public Result error(BindException e){
        //e.printStackTrace();
        LOG.error("校验异常：{}", e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return Result.error().code(ResultCode.PARAM_VAL_ERROR.getCode())
                .message(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    /**
     * 处理业务异常,我们自己定义的异常
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result error(BusinessException e){
        //e.printStackTrace();
        LOG.error("业务异常：{}", e.getErrMsg());
        return Result.error().code(e.getCode())
                .message(e.getErrMsg());
    }

}
