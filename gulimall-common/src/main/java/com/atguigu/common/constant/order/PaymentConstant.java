package com.atguigu.common.constant.order;

import lombok.Data;

/**
 * 支付枚举
 * @Author: wanzenghui
 * @Date: 2022/1/4 22:47
 */
public class PaymentConstant {

    // 支付成功异步跳转的地址（内网穿透）
    public static final String SYSTEM_URL = "http://124.223.7.41:8888";

    /**
     * 支付类型
     */
    public enum PayType {
        PAYPAL(101, "paypalStrategy"),
        PAYPAL_HK(102, "paypalHKStrategy"),
        PAYPAL_GLOB(103, "paypalGlobalStrategy"),
        ALI_PAY(201, "aliPayStrategy"),
        ALI_PAY_HK(202, "aliPayHKStrategy"),
        ALI_PAY_GLOB(203, "aliPayGlobalStrategy"),
        WECHAT_PAY(301, "weChatPayStrategy"),
        WECHAT_PAY_HK(302, "weChatPayHKStrategy"),
        WECHAT_PAY_GLOB(303, "weChatPayGlobalStrategy"),
        BANK(401, "bankPayStrategy"),
        FACE_PAY(501, "bankPayStrategy"),
        ;

        private Integer code;
        private String strategyBeanId;


        PayType(int code, String strategyBeanId) {
            this.code = code;
            this.strategyBeanId = strategyBeanId;
        }

        public static PayType getByCode(Integer code) {
            for (PayType em : values()) {
                if (em.getCode().equals(code))
                    return em;
            }
            return null;
        }

        public static boolean isValid(Integer code) {
            if (code == null)
                return false;
            for (PayType type : values()) {
                if (type.code.equals(code))
                    return true;
            }
            return false;
        }

        public Integer getCode() {
            return this.code;
        }

        public String getStrategyBeanId() {
            return this.strategyBeanId;
        }

    }

    /**
     * 业务详情
     */
    public enum PayBusinessType {
        ORDER(201001, "订单支付"),
        ;

        private Integer code;
        private String desc;

        PayBusinessType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 业务详情
     */
    public enum PayBusinessDetailType {
        ALI_ORDER(201, 201001, "订单支付", "/payed/ali/notify", "http://member.gulimall.com/memberOrder.html"),
        ALI_ORDER_HK(202, 201001, "订单支付", "/payed/ali/hk/notify", "http://member.gulimall.com/memberOrder.html"),
        ;

        private Integer code;// 支付类型code
        private Integer businessCode;// 业务类型code
        private String desc;// 描述
        private String notifyUrl;// 异步回调地址
        private String returnUrl;// 同步回调地址

        PayBusinessDetailType(Integer code, Integer businessCode, String desc, String notifyUrl, String returnUrl) {
            this.code = code;
            this.businessCode = businessCode;
            this.desc = desc;
            this.notifyUrl = notifyUrl;
            this.returnUrl = returnUrl;
        }

        public static PayBusinessDetailType getByCodeAndBusinessCode(Integer code, Integer businessCode) {
            for (PayBusinessDetailType type : values()) {
                if (type.getCode().equals(code) && type.getBusinessCode().equals(businessCode)) {
                    return type;
                }
            }
            return null;
        }

        public Integer getCode() {
            return code;
        }

        public Integer getBusinessCode() {
            return businessCode;
        }

        public String getDesc() {
            return desc;
        }

        public String getNotifyUrl() {
            return notifyUrl;
        }

        public String getReturnUrl() {
            return returnUrl;
        }
    }
}