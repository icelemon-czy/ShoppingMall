package com.shoppingmall.service;

import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.vo.CartVo;

public interface ICartService {
    ServerResponse<CartVo> addProduct(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> updateProduct(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> deleteProducts(Integer userId,String productIds);

    ServerResponse<CartVo> listCart(Integer userId);

    ServerResponse<CartVo> selectOrUnselect(Integer userId,Integer productId,Integer checked);

    ServerResponse<Integer> getCartProductCount(Integer userId);
}
