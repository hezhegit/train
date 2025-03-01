package com.hezhe.train.common.resp;

/**
 * 规定:
 * #200表示成功
 * #1001～1999 区间表示参数错误
 * #2001～2999 区间表示用户错误
 * #3001～3999 区间表示接口异常
 * #后面对什么的操作自己在这里注明就行了
 */
public enum ResultCode implements CustomizeResultCode{
    /* 成功 */
    SUCCESS(200, "成功"),

    /* 默认失败 */
    COMMON_FAIL(999, "失败"),

    /* 参数错误：1000～1999 */
    PARAM_NOT_VALID(1001, "参数无效"),
    PARAM_IS_BLANK(1002, "参数为空"),
    PARAM_TYPE_ERROR(1003, "参数类型错误"),
    PARAM_NOT_COMPLETE(1004, "参数缺失"),
    PARAM_VAL_ERROR(1005, "校验错误"),

    /* 用户错误 */
    USER_NOT_LOGIN(2001, "用户未登录"),
    USER_ACCOUNT_EXPIRED(2002, "账号已过期"),
    USER_CREDENTIALS_ERROR(2003, "密码错误"),
    USER_CREDENTIALS_EXPIRED(2004, "密码过期"),
    USER_ACCOUNT_DISABLE(2005, "账号不可用"),
    USER_ACCOUNT_LOCKED(2006, "账号被锁定"),
    USER_ACCOUNT_NOT_EXIST(2007, "账号不存在"),
    USER_ACCOUNT_ALREADY_EXIST(2008, "账号已存在"),
    USER_ACCOUNT_USE_BY_OTHERS(2009, "账号下线"),
    USER_MOBILE_NOT_EXIST(2010, "请先获取短信验证码"),
    USER_MOBILE_CODE_ERROR(2011, "验证码错误"),

    /* 车站错误 */
    STATION_NOT_EXIST(3007, "车站不存在"),
    STATION_ALREADY_EXIST(3008, "车站已存在"),

    /* 火车车站错误 */
    TRAIN_STATION_INDEX_ALREADY_EXIST(4007, "同车次站序已存在"),
    TRAIN_STATION_NAME_ALREADY_EXIST(4008, "同车次站名已存在"),

    /* 车次错误 */
    TRAIN_NOT_EXIST(5007, "车次编号不存在"),
    TRAIN_ALREADY_EXIST(5008, "车次编号已存在"),

    /* 车厢错误 */
    TRAIN_CARRIAGE_INDEX_ALREADY_EXIST(6007, "同车次厢号已存在"),

    /* 业务错误 */
    NO_PERMISSION(3001, "没有权限"),

    /* 订单错误 */
    CONFIRM_ORDER_TICKET_COUNT_ERROR(7001, "余票不足"),
    CONFIRM_ORDER_EXCEPTION(7002, "服务器繁忙，请稍后重试！"),


    /*运行时异常*/
    ARITHMETIC_EXCEPTION(9001,"算数异常");

    private Integer code;

    private String message;

    ResultCode(Integer code,String message){
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

