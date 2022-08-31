package com.shoppingmall.service.implement;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.dao.ShippingMapper;
import com.shoppingmall.pojo.Shipping;
import com.shoppingmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("iShippingService")
public class ShippingServiceImplement implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if(rowCount > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("Shipping Address create Success",result);
        }
        return ServerResponse.createByErrorMessage("Shipping Address create fail");
    }

    public ServerResponse<String> delete(Integer userId, Integer shippingId){
        int resultCount = shippingMapper.deleteByShippingIdUserId(shippingId,userId);
        if(resultCount>0){
            return ServerResponse.createBySuccess("Delete Shipping Address success");
        }
        return ServerResponse.createByErrorMessage("Delete Shipping Address fail");
    }

    public ServerResponse update(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByShipping(shipping);
        if(rowCount > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("Shipping Address update Success",result);
        }
        return ServerResponse.createByErrorMessage("Shipping Address update fail");
    }

    public ServerResponse select(Integer userId, Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(shippingId,userId);
        if(shipping == null){
            return ServerResponse.createByErrorMessage("Shipping Address does not exists");
        }
        return ServerResponse.createBySuccess("Shipping Address find !",shipping);
    }

    public ServerResponse<PageInfo> list(Integer userId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
