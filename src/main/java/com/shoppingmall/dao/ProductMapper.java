package com.shoppingmall.dao;

import com.shoppingmall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    List<Product> selectProductList();

    List<Product> selectProductByNameAndId(@Param("name") String name,@Param("id") Integer id);

    List<Product> selectProductByNameAndCategoryIds(@Param("name") String name,@Param("categoryIdList")List<Integer> categoryIdList);
}