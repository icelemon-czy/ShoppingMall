package com.shoppingmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

public class Const {
    public static final String Current_User = "current_User";

    public interface Role{
        int Role_Customer = 0; // Customer
        int Role_Admin = 1;
    }

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    public interface ShoppingCart{
        int Checked = 1;
        int UnChecked = 0;

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public enum ProductStatusEnum{
        ON_SALE(1,"ON_SALE");
        private String value;
        private int code;
        ProductStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }

    }

    public enum OrderStatusEnum{
        CANCELED(0,"CANCELED"),
        NO_PAY(10,"NO_PAY"),
        PAID(20,"PAID"),
        SHIPPED(40,"SHIPPED"),
        ORDER_SUCCESS(50,"ORDER_SUCCESS"),
        ORDER_CLOSED(60,"ORDER_CLOSED")
        ;
        private String value;
        private int code;

        OrderStatusEnum(int code ,String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static OrderStatusEnum find(int code){
            for(OrderStatusEnum orderStatusEnum: values()){
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("Do not have such order status!");
        }

    }

    public interface AlipayCallback{
        String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";

        String TRADE_CLOSED = "TRADE_CLOSED";
        String TRADE_SUCCESS = "TRADE_SUCCESS";

        String TRADE_FINISHED = "TRADE_FINISHED";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum PayPlatformEnum{
        ALIPAY(1,"ALIPAY");
        private String value;
        private int code;

        PayPlatformEnum(int code ,String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    public enum PaymentTypeEnum{
        ONLINE_PAY(1,"ONLINE");
        private String value;
        private int code;

        PaymentTypeEnum(int code ,String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static PaymentTypeEnum find(int code){
            for(PaymentTypeEnum paymentTypeEnum: values()){
                if(paymentTypeEnum.getCode() == code){
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("Do not have such payment type!");
        }
    }

    public static final String Email = "Email";
    public static final String Username = "Username";
}
