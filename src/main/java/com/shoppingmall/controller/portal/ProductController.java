package com.shoppingmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.shoppingmall.common.Const;
import com.shoppingmall.common.ResponseCode;
import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.pojo.User;
import com.shoppingmall.service.IProductService;
import com.shoppingmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/product/")
public class ProductController {
    @Autowired
    private IProductService iProductService;

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId){
        return iProductService.getProductDetailByClient(productId);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value =  "keyword",required = false)String keyword,
                                         @RequestParam(value =  "categoryId",required = false)Integer categoryId,
                                         @RequestParam(value =  "pageNum",defaultValue = "1")int pageNum,
                                         @RequestParam(value =  "pageSize",defaultValue = "10")int pageSize,
                                         @RequestParam(value =  "orderBy",defaultValue = "")String orderBy){
        return iProductService.getProductByKeywordCategory(keyword, categoryId, pageNum, pageSize, orderBy);
    }
}
