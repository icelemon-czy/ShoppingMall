package com.shoppingmall.service.implement;

import com.alipay.api.AlipayResponse;
import com.alipay.api.domain.OrderItem;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shoppingmall.common.Const;
import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.dao.ItemMapper;
import com.shoppingmall.dao.OrderMapper;
import com.shoppingmall.dao.PayInfoMapper;
import com.shoppingmall.pojo.Item;
import com.shoppingmall.pojo.Order;
import com.shoppingmall.pojo.PayInfo;
import com.shoppingmall.service.IOrderService;
import com.shoppingmall.util.BigDecimalUtil;
import com.shoppingmall.util.DateTimeUtil;
import com.shoppingmall.util.FTPUtil;
import com.shoppingmall.util.PropertiesUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("iOrderService")
public class OrderServiceImplement implements IOrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImplement.class);
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    public ServerResponse pay(Integer userId,Long orderNo,String path){
        Map<String,String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("Order does not belong to the user.");
        }
        resultMap.put("orderNo",order.getOrderNo().toString());


        String outTradeNo = order.getOrderNo().toString();

        // Subject of Order,to describe the goal of payment from user, for example: “xxx Brand, face to face QR code way to pay”
        String subject = new StringBuilder().append("ShoppingMall QR code pay, orderNo:").append(outTradeNo).toString();

        String totalAmount = order.getPayment().toString();

        String undiscountableAmount = "0";

        // The seller's Alipay account ID, which is used to support payment to different collection accounts under one contract account (payment to the Alipay account corresponding to sellerId)
        // If it is empty, the default is the PID of the merchant that signed with Alipay, that is, the PID corresponding to the appid
        String sellerId = "";

        // Order description, you can give a detailed description of the transaction or product, for example, "buy 2 items for a total of 15.00 usd"
        String body = new StringBuilder().append("Order:").append(outTradeNo).append(", Total Amount:").append(totalAmount).toString();

        String operatorId = "operator_id null";

        String storeId = "store_id null";

        // Business expansion parameters, currently you can add the system provider ID assigned by Alipay (through the setSysServiceProviderId method).
        // For details, please contact Alipay technical support
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // time out
        String timeoutExpress = "120m";

        // Product details list, fill in the details of the purchased product
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<Item> orderItemList = itemMapper.selectByOrderNoUserId(orderNo, userId);

        for(Item item : orderItemList){
            // Create a product information, the parameter meanings are product id, name, unit price (unit is cent), quantity, if you need to add product category
            GoodsDetail goods = GoodsDetail.newInstance(item.getProductId().toString(), item.getProductName(),
                    BigDecimalUtil.mul(item.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(),
                    item.getQuantity());
            goodsDetailList.add(goods);
        }

        // Create a scan code payment request builder and set request parameters
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))// Alipay callback path
                .setGoodsDetailList(goodsDetailList);

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("Alipay pre-order success: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);
                // QR code will be uploaded to the path we specify
                // Test whether folder path exist,if not we create such folder.
                File folder = new File(path);
                if(!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // Generate QR code path
                String QRPath = String.format(path+"/qr-%s.png", response.getOutTradeNo());
                // String QRFileName = String.format("qr-%s.png", response.getOutTradeNo());
                /**
                 * Parameters:
                 * contents - the contents
                 * width - the width
                 * imgPath - the img path
                 * Returns:
                 * the qr code imge
                 */
                ZxingUtils.getQRCodeImge(response.getQrCode(),256,QRPath);
                File targetFile = new File(QRPath);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                }catch (IOException e){
                    logger.error("Upload QR Image exception! ",e);
                }
                logger.info("QRPath:" + QRPath);
                String QRURL = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                resultMap.put("QRURL",QRURL);
                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                logger.error("Alipay pre-order fail !");
                return ServerResponse.createByErrorMessage("Alipay pre-order fail !");
            case UNKNOWN:
                logger.error("The system is abnormal, the pre-order status is unknown !");
                return ServerResponse.createByErrorMessage("The system is abnormal, the pre-order status is unknown !");
            default:
                logger.error("Unsupported transaction status, transaction returns exception!");
                return ServerResponse.createByErrorMessage("Unsupported transaction status, transaction returns exception!");
        }
    }

    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    public ServerResponse alipayCallback(Map<String,String> params){
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");

        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("Order is not from shopping mall, omit the callback!");
        }
        // Already Paid
        if(order.getStatus()>= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess("Alipay repeated callback ！ Order has already paid.");
        }
        if(Const.AlipayCallback.TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(orderNo);
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("User does not have order");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode() ){
            return ServerResponse.createBySuccess();
        }else{
            return ServerResponse.createByError();
        }
    }
}
