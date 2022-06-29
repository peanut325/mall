package com.atguigu.common.exception;

/**
 * 错误码和错误信息定义类
 * 1.错误码定义规则为5为数字
 * 2.前两位表示业务场景，最后三位表示错误码。例如:100001。10:通用001:系统未知
 * 异常
 * 3.维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表:
 * 10:通用
 * 001:参数格式校验
 * 002:短信验证频率太高
 * 11:商品
 * 12:订单
 * 13:购物车
 * 14:物流
 * 21:库存
 */
public enum BizCodeEnum {
    UNKNOW_EXEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，请稍后再试"),
    TO_MANY_REQUEST(10002, "请求流量过大，请稍后再试"),

    IDEMPOTENT_TOKEN_CREATE_EXCEPTION(10003, "令牌创建失败"),
    IDEMPOTENT_TOKEN_VERIFY_EXCEPTION(10003, "令牌校验失败"),

    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    USER_EXIST_EXCEPTION(15001, "存在相同的用户"),
    PHONE_EXIST_EXCEPTION(15002, "存在相同的手机号"),
    LOGINACCT_PASSWORD_EXCEPTION(15003, "账号或密码错误"),
    NO_STOCK_EXCEPTION(21000, "商品库存不足");


    private int code;
    private String msg;


    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    BizCodeEnum() {
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
