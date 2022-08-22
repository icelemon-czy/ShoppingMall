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

    public static final String Email = "Email";
    public static final String Username = "Username";
}
