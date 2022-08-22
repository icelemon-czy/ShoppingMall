package com.shoppingmall.service;

import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.pojo.Category;

import java.util.List;

public interface ICategoryService {
    ServerResponse<String> addCategory(String categoryName, Integer parentId);
    ServerResponse<String> setCategory(String newCategoryName,Integer id);
    ServerResponse<List<Category>> getChildrenCategory(Integer parentId);
    ServerResponse<List<Integer>> getAllDescendantCategory(Integer parentId);
}
