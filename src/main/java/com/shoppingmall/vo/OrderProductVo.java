package com.shoppingmall.vo;

import java.math.BigDecimal;
import java.util.List;

public class OrderProductVo {
    private List<ItemVo> orderItemVoList;
    private BigDecimal productTotalPrice;
    private String imageHost;

    public List<ItemVo> getOrderItemVoList() {
        return orderItemVoList;
    }

    public void setOrderItemVoList(List<ItemVo> orderItemVoList) {
        this.orderItemVoList = orderItemVoList;
    }

    public BigDecimal getProductTotalPrice() {
        return productTotalPrice;
    }

    public void setProductTotalPrice(BigDecimal productTotalPrice) {
        this.productTotalPrice = productTotalPrice;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
