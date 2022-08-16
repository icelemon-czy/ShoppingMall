package com.shoppingmall.service;

import com.github.pagehelper.PageInfo;
import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.pojo.Product;
import com.shoppingmall.vo.ProductDetailVo;

public interface IProductService {
    ServerResponse addOrUpdateProduct(Product product);
    ServerResponse<String> setSaleStatus(Integer productId,Integer status);
    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductList(int pageNum, int pageSize);
    ServerResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize);
}
