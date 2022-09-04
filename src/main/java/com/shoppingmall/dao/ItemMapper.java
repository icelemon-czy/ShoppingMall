package com.shoppingmall.dao;

import com.shoppingmall.pojo.Item;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Item record);

    int insertSelective(Item record);

    Item selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Item record);

    int updateByPrimaryKey(Item record);

    List<Item> selectByOrderNoUserId(@Param("orderNo")Long orderNo,@Param("userId") Integer userId);

    void batchInsert(@Param("orderItemList") List<Item> orderItemList);
}