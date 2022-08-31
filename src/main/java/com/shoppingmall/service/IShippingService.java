package com.shoppingmall.service;

import com.github.pagehelper.PageInfo;
import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.pojo.Shipping;

public interface IShippingService {
    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse<String> delete(Integer userId, Integer shippingId);

    ServerResponse update(Integer userId, Shipping shipping);

    ServerResponse select(Integer userId, Integer shippingId);

    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);
}
