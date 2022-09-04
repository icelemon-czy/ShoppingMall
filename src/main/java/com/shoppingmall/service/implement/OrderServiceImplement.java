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
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shoppingmall.common.Const;
import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.dao.*;
import com.shoppingmall.pojo.*;
import com.shoppingmall.service.IOrderService;
import com.shoppingmall.util.BigDecimalUtil;
import com.shoppingmall.util.DateTimeUtil;
import com.shoppingmall.util.FTPUtil;
import com.shoppingmall.util.PropertiesUtil;
import com.shoppingmall.vo.ItemVo;
import com.shoppingmall.vo.OrderProductVo;
import com.shoppingmall.vo.OrderVo;
import com.shoppingmall.vo.ShippingVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service("iOrderService")
public class OrderServiceImplement implements IOrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImplement.class);
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private ShippingMapper shippingMapper;
    // --------------- Create Order --------------
    public ServerResponse createOrder(Integer userId,Integer shippingId){
        List<ShoppingCart> cartList = shoppingCartMapper.selectCheckedCartByUserId(userId);
        ServerResponse serverResponse = getCartOrderItem(userId,cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<Item> orderItemList = (List<Item>) serverResponse.getData();
        if(CollectionUtils.isEmpty(orderItemList)){
            return ServerResponse.createByErrorMessage("Shopping Cart is empty!");
        }
        BigDecimal payment = getOrderTotalPrice(orderItemList);
        Order order = assembleOrder(userId,shippingId,payment);
        if(order == null){
            return ServerResponse.createByErrorMessage("Create order fail!");
        }
        for(Item item : orderItemList){
            item.setOrderNo(order.getOrderNo());
        }
        // MyBatis batch insert
        itemMapper.batchInsert(orderItemList);

        // Reduce Product Stock
        reduceProductStock(orderItemList);

        // Clean checked shopping cart item.
        cleanShoppingCart(cartList);

        // Return VO to the front
        OrderVo orderVo = assembleOrderVo(order,orderItemList);

        return ServerResponse.createBySuccess(orderVo);
    }

    private void reduceProductStock(List<Item> orderItemList){
        for(Item item : orderItemList){
            Product product = productMapper.selectByPrimaryKey(item.getProductId());
            Product update = new Product();
            update.setId(product.getId());
            update.setStock(product.getStock() - item.getQuantity());
            productMapper.updateByPrimaryKeySelective(update);
        }
    }

    private void cleanShoppingCart(List<ShoppingCart> cartList){
        for(ShoppingCart shoppingCart:cartList){
            shoppingCartMapper.deleteByPrimaryKey(shoppingCart.getId());
        }
    }

    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){
        Order order = new Order();
        // Generate Order number
        Long orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPayment(payment);
        order.setShippingId(shippingId);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        int rowCount = orderMapper.insert(order);
        if(rowCount == 0){
            return null; // Create fail;
        }
        return order;
    }

    private OrderVo assembleOrderVo(Order order,List<Item> orderItemList){
        // Order Part
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.find(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.find(order.getStatus()).getValue());
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));

        // Shipping Part
        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping != null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }

        // Image Host Part
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        // Order Item Part
        List<ItemVo> orderItemVoList = Lists.newArrayList();
        for(Item item : orderItemList){
            orderItemVoList.add(assembleItemVo(item));
        }
        orderVo.setOrderItemVoList(orderItemVoList);

        return orderVo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shippingVo.getReceiverPhone());
        return shippingVo;
    }

    private ItemVo assembleItemVo(Item item){
        ItemVo orderItemVo = new ItemVo();
        orderItemVo.setOrderNo(item.getOrderNo());
        orderItemVo.setProductId(item.getProductId());
        orderItemVo.setProductName(item.getProductName());
        orderItemVo.setProductImage(item.getProductImage());
        orderItemVo.setCurrentUnitPrice(item.getCurrentUnitPrice());
        orderItemVo.setQuantity(item.getQuantity());
        orderItemVo.setTotalPrice(item.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(item.getCreateTime()));
        return orderItemVo;
    }

    private long generateOrderNo(){
        // Generate order number based on timestamp.
        long currentTime = System.currentTimeMillis();
        return currentTime+ new Random().nextInt(1000);
    }

    private BigDecimal getOrderTotalPrice(List<Item> orderItemList){
        BigDecimal payment = new BigDecimal("0");
        for(Item item : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),item.getTotalPrice().doubleValue());
        }
        return payment;
    }

    private ServerResponse<List<Item>> getCartOrderItem(Integer userId,List<ShoppingCart> cartList){
        List<Item> itemList = Lists.newArrayList();
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("Shopping Cart is empty!");
        }
        // Validate Shopping Cart data, make sure product is on sale
        // and product quantity is below or equal to stock
        for(ShoppingCart cart : cartList){
            Item item = new Item();
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            // Validation process
            if(Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()){
                return ServerResponse.createByErrorMessage("Product"+product.getName()+" is not on sale!");
            }
            if(cart.getQuantity() > product.getStock()){
                return ServerResponse.createByErrorMessage("Product"+product.getName()+"Insufficient stock!");
            }
            // Fill in item
            item.setUserId(userId);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getMainImage());
            item.setCurrentUnitPrice(product.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setTotalPrice(BigDecimalUtil.mul(cart.getQuantity(), product.getPrice().doubleValue()));

            itemList.add(item);
        }
        return ServerResponse.createBySuccess(itemList);
    }

    // ----------------Cancel Order-----------------
    public ServerResponse<String> cancel(Long orderNo,Integer userId){
        Order order = orderMapper.selectByUserIdOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("User does not have this order!");
        }
        if(order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()){
            return ServerResponse.createByErrorMessage("User has paid and cannot cancel the order!");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        // TODO: product stock !!!!!!!
        int rowCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if(rowCount>0){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();


    }

    public ServerResponse getOrderCartProduct(Integer userId){
        OrderProductVo orderProductVo = new OrderProductVo();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectCheckedCartByUserId(userId);
        ServerResponse serverResponse = getCartOrderItem(userId,shoppingCartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<Item> orderItemList = (List<Item>) serverResponse.getData();
        List<ItemVo> orderItemVoList = Lists.newArrayList();
        for(Item orderItem : orderItemList){
            orderItemVoList.add(assembleItemVo(orderItem));
        }
        BigDecimal payment = getOrderTotalPrice(orderItemList);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderProductVo.setProductTotalPrice(payment);
        return ServerResponse.createBySuccess(orderProductVo);
    }

    // --------------Order Detail ----------------
    public ServerResponse<OrderVo> getOrderDetail(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("User does not have such order!");
        }
        List<Item> orderItemList = itemMapper.selectByOrderNoUserId(orderNo, userId);
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    public ServerResponse<PageInfo> getOrderList(Integer userId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        // Convert Order to Order Vo
        List<OrderVo> orderVoList = assembleOrderVoList(orderList,userId);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for(Order order : orderList){
            List<Item> orderItemList;
                // Admin query, Do not need the userId
            if(userId == null){
                orderItemList = itemMapper.selectByOrderNo(order.getOrderNo());
            }else{
                // User
                orderItemList = itemMapper.selectByOrderNoUserId(order.getOrderNo(),userId);
            }
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }



    // --------------------------AliPay Related methods pay and callback------------------
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

    // ----------------------------BackEnd Manage-----------------------------
    public ServerResponse<PageInfo> manageGetOrderList(int pageNum,int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList =  orderMapper.selectAllOrder();
        // Convert Order to Order Vo
        List<OrderVo> orderVoList = assembleOrderVoList(orderList,null);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<OrderVo> manageDetail(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("Does not have such order!");
        }
        List<Item> orderItemList = itemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    public ServerResponse<PageInfo> manageSearch(int pageNum,int pageSize,Long orderNo){
        PageHelper.startPage(pageNum, pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("Does not have such order!");
        }
        List<Item> orderItemList = itemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
        pageInfo.setList(Lists.newArrayList(orderVo));
        return ServerResponse.createBySuccess(pageInfo);
    }
}
