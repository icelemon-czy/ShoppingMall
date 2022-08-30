package com.shoppingmall.service.implement;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.shoppingmall.common.Const;
import com.shoppingmall.common.ResponseCode;
import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.dao.ProductMapper;
import com.shoppingmall.dao.ShoppingCartMapper;
import com.shoppingmall.pojo.Product;
import com.shoppingmall.pojo.ShoppingCart;
import com.shoppingmall.service.ICartService;
import com.shoppingmall.util.BigDecimalUtil;
import com.shoppingmall.util.PropertiesUtil;
import com.shoppingmall.vo.CartProductVo;
import com.shoppingmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service("iCartService")
public class CartServiceImplement implements ICartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private ProductMapper productMapper;

    public ServerResponse<CartVo> addProduct(Integer userId,Integer productId,Integer count){
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        ShoppingCart cart  = shoppingCartMapper.selectCartByUserIdProductId(userId, productId);
        if(cart == null){
            // User's shopping cart does not contain the product yet.
            ShoppingCart cartItem = new ShoppingCart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.ShoppingCart.Checked);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            shoppingCartMapper.insert(cartItem);
        }else{
            // User's shopping cart already contained the product
            count += cart.getQuantity();
            cart.setQuantity(count);
            // Update product quantity in shopping cart
            shoppingCartMapper.updateByPrimaryKeySelective(cart);
        }
        return listCart(userId);
    }

    public ServerResponse<CartVo> updateProduct(Integer userId, Integer productId, Integer count){
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        ShoppingCart cart  = shoppingCartMapper.selectCartByUserIdProductId(userId, productId);
        if(cart != null){
            cart.setQuantity(count);
            shoppingCartMapper.updateByPrimaryKeySelective(cart);
        }
        return listCart(userId);
    }

    public ServerResponse<CartVo> deleteProducts(Integer userId,String productIds){
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productList)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        shoppingCartMapper.deleteByUserIdProductIds(userId,productList);
        return listCart(userId);
    }

    public ServerResponse<CartVo> listCart(Integer userId){
        CartVo cartVo = getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> selectOrUnselect(Integer userId,Integer productId,Integer checked){
        shoppingCartMapper.checkedOrUncheckedProduct(userId,productId,checked);
        return listCart(userId);
    }

    public ServerResponse<Integer> getCartProductCount(Integer userId){
        if(userId == null){
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(shoppingCartMapper.selectCartProductCount(userId));
    }



    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        BigDecimal cartTotalPrice = new BigDecimal("0");
        if(!CollectionUtils.isEmpty(shoppingCartList)){
            for(ShoppingCart cartItem : shoppingCartList){
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getId());
                if(product != null){
                    cartProductVo.setMainImage(product.getMainImage());
                    cartProductVo.setName(product.getName());
                    cartProductVo.setSubtitle(product.getSubtitle());
                    cartProductVo.setStatus(product.getStatus());
                    cartProductVo.setPrice(product.getPrice());
                    cartProductVo.setStock(product.getStock());
                    // Check Stock, quantity in shopping cart can not be greater than stock
                    int buyLimitCount = 0;
                    if(product.getStock() >= cartItem.getQuantity()){
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.ShoppingCart.LIMIT_NUM_SUCCESS);
                    }else{
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.ShoppingCart.LIMIT_NUM_FAIL);
                        // Update quantity
                        ShoppingCart updateQuantity = new ShoppingCart();
                        updateQuantity.setId(cartItem.getId());
                        updateQuantity.setQuantity(product.getStock());
                        shoppingCartMapper.updateByPrimaryKeySelective(updateQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    // Calculate total price for specify item
                    cartProductVo.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),buyLimitCount));
                    cartProductVo.setChecked(cartItem.getChecked());
                }

                if(cartItem.getChecked() == Const.ShoppingCart.Checked){
                    cartTotalPrice =  BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null) return false;
        return shoppingCartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
    }
}
