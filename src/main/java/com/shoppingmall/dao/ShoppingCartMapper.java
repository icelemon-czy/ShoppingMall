package com.shoppingmall.dao;

import com.shoppingmall.pojo.ShoppingCart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShoppingCartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ShoppingCart record);

    int insertSelective(ShoppingCart record);

    ShoppingCart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ShoppingCart record);

    int updateByPrimaryKey(ShoppingCart record);

    ShoppingCart selectCartByUserIdProductId(@Param("userId") Integer userId,@Param("productId") Integer productId);

    List<ShoppingCart> selectCartByUserId(Integer userId);

    int selectCartProductCheckedStatusByUserId(Integer userId);

    int deleteByUserIdProductIds(@Param("userId") Integer userId,@Param("productIdList") List<String> productIdList);

    int checkedOrUncheckedProduct(@Param("userId") Integer userId,@Param("productId")Integer productId,@Param("checked") Integer checked);

    int selectCartProductCount(Integer userId);

    List<ShoppingCart> selectCheckedCartByUserId(Integer userId);
}