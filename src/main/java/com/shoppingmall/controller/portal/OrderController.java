package com.shoppingmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.shoppingmall.common.Const;
import com.shoppingmall.common.ResponseCode;
import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.pojo.User;
import com.shoppingmall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/order/")
public class OrderController {
    @Autowired
    private IOrderService iOrderService;

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.Current_User);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(user.getId(), orderNo, path);
    }

    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        Map<String,String> params = Maps.newHashMap();
        Map requestParams = request.getParameterMap();
        for(Iterator iter = requestParams.keySet().iterator(); iter.hasNext();){
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            StringBuilder valueStr = new StringBuilder();
            for(int i =0;i< values.length;i++){
                if(i== values.length-1){
                    valueStr.append(values[i]);
                }else{
                    valueStr.append(values[i]).append(',');
                }
            }
            params.put(name,valueStr.toString());
        }
        // Sign : signature String(256)
        logger.info("Alipay callback,sign:{},trade_status:{},parameter:{}",params.get("sign"),params.get("trade_status"),params.toString());

        // First Step: Validate Callback (1.whether it is sent by alipay, 2.Avoid repeat callback)
        params.remove("sign_type");
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if(!alipayRSACheckedV2){
                return ServerResponse.createByErrorMessage("Invalid request, validation fail!");
            }
        } catch (AlipayApiException e) {
            logger.error("Alipay callback exception ",e);
        }

        // Todo validate alipay callback data.

        ServerResponse serverResponse = iOrderService.alipayCallback(params);
        if(serverResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }else{
            return Const.AlipayCallback.RESPONSE_FAILED;
        }
    }

    @RequestMapping("queryOrderPayStatus.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.Current_User);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if(serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(Boolean.TRUE);
        }else{
            return ServerResponse.createBySuccess(Boolean.FALSE);
        }
    }

}
