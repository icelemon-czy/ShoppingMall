package com.shoppingmall.service.implement;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.dao.CategoryMapper;
import com.shoppingmall.pojo.Category;
import com.shoppingmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImplement implements ICategoryService {
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImplement.class);
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse<String> addCategory(String categoryName,Integer parentId){
        if(StringUtils.isBlank(categoryName) || parentId == null){
            return ServerResponse.createByErrorMessage("Parameter invalid !");
        }
        Category category= new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        int rowCount = categoryMapper.insert(category);
        if(rowCount>0){
            return ServerResponse.createBySuccessMessage("Add category success !");
        }
        return ServerResponse.createByErrorMessage("Add category fail !");
    }
    @Override
    public ServerResponse<String> setCategory(String newCategoryName,Integer id){
        if(StringUtils.isBlank(newCategoryName) || id== null){
            return ServerResponse.createByErrorMessage("Parameter invalid !");
        }
        Category category = new Category();
        category.setId(id);
        category.setName(newCategoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount > 0){
            return ServerResponse.createBySuccessMessage("Update Category Name success !");
        }
        return ServerResponse.createByErrorMessage("Update Category Name fail !");
    }
    @Override
    public ServerResponse<List<Category>> getChildrenCategory(Integer parentId){
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(parentId);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info(parentId+ " has no children category!");
        }
        return ServerResponse.createBySuccess(categoryList);
    }
    @Override
    public ServerResponse<List<Integer>> getAllDescendantCategory(Integer parentId){
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet,parentId);
        List<Integer> categoryIdList = Lists.newArrayList();
        if(parentId !=null){
            for(Category childCategory: categorySet){
                categoryIdList.add(childCategory.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }
    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null){
            categorySet.add(category);
        }
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for(Category childCategory : categoryList){
            findChildCategory(categorySet, childCategory.getId());
        }
        return categorySet;
    }
}
