package com.shoppingmall.dao;

import com.shoppingmall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int deleteByShippingIdUserId(@Param("id") Integer id,@Param("userId") Integer userId);

    int updateByShipping(Shipping record);

    Shipping selectByShippingIdUserId(@Param("id") Integer id,@Param("userId") Integer userId);

    List<Shipping> selectByUserId(Integer userId);
}