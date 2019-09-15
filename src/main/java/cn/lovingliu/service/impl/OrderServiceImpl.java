package cn.lovingliu.service.impl;

import cn.lovingliu.common.Const;
import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.dao.*;
import cn.lovingliu.pojo.*;
import cn.lovingliu.service.IOrderService;
import cn.lovingliu.util.BigDecimalUtil;
import cn.lovingliu.util.DateTimeUtil;
import cn.lovingliu.util.FTPUtil;
import cn.lovingliu.util.PropertiesUtil;
import cn.lovingliu.vo.OrderItemVo;
import cn.lovingliu.vo.OrderProductVo;
import cn.lovingliu.vo.OrderVo;
import cn.lovingliu.vo.ShippingVo;
import com.alipay.api.AlipayResponse;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-12
 */
@Service("orderService")
public class OrderServiceImpl implements IOrderService {
    private static  AlipayTradeService tradeService;
    static {

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;


    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    public ServerResponse orderPay(Long orderNo,Integer userId,String path){
        Map<String,String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            // 该用户不存在该订单
            return ServerResponse.createByErrorMessage("该用户不存在该订单");
        }
        resultMap.put("orderNo",order.getOrderNo().toString());

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = orderNo.toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = "LovingLiu扫码支付,订单号: "+outTradeNo;

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuffer().append("订单: "+ outTradeNo).append(" 合计购买金额: "+totalAmount+"元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId,orderNo);
        for (OrderItem orderItem: orderItemList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            // 支付宝金额的单位是分 BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue()
            GoodsDetail good = GoodsDetail.newInstance(orderItem.getId().toString(), orderItem.getProductName(), BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(),orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(good);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);


        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);


                File folder = new File(path);
                if(!folder.exists()){
                   folder.setWritable(true);
                   folder.mkdirs();
                }
                /**
                 * 先在服务器发布目录生成零时文件
                 */
                // 需要修改为运行机器上的路径
                String qrcodePath = String.format(path+"/"+"qr-%s.png",
                        response.getOutTradeNo());
                String qrcodeFileName = String.format("qr-%s.png",response.getOutTradeNo());
                logger.info("filePath:" + qrcodePath);
                // 将内容response.getQrCode()生成长宽均为256的图片，图片路径由qrcodePath指定
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrcodePath);

                /**
                 * 拿到临时文件 上传到vsftp服务器 并删除临时文件
                 */
                File targetFile = new File(path,qrcodeFileName);
                try{
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                }catch (Exception e){
                    logger.error("二维码上传到vsftp异常");
                }
                logger.info("qrcodePath === "+qrcodePath);

                String qrcodeUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                resultMap.put("qrcodeUrl",qrcodeUrl);
                return ServerResponse.createBySuccess(resultMap);
            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    /**
     * @Desc 阿里支付回调的方法
     * @Author LovingLiu
    */
    public ServerResponse aliCallBack(Map<String,String> params){
        Long orderNo = Long.valueOf(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");// 交易号
        String tradeStatus = params.get("trade_status");// 交易状态
        Order  order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if(order.getStatus() >= Const.OrderPayStatusEnum.PAID.getStatus()){
            return ServerResponse.createBySuccessMessage("订单一斤支付,不要再重复调用啦");
        }
        if(Const.AlipayCallBack.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));// 付款时间 订单
            order.setStatus(Const.OrderPayStatusEnum.PAID.getStatus());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        PayInfo payInfo = new PayInfo();
        payInfo.setId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getStatus());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId,Long orderId){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderId);
        if(order == null){
            return ServerResponse.createByErrorMessage("该用户不存在该订单");
        }
        if(order.getStatus() >= Const.OrderPayStatusEnum.PAID.getStatus()){
            return ServerResponse.createBySuccessMessage("付款成功");
        }
        return ServerResponse.createBySuccessMessage("付款失败");
    }
    /**
     * @Desc 创建订单
     * @Author LovingLiu
    */
    public ServerResponse createOrder(Integer userId,Integer shippingId){
        // 从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        // 计算订单总价
        ServerResponse<List<OrderItem>> serverResponse = this.getCartOrderItem(userId,cartList);
        if(!serverResponse.ifSuccess()){
            return  serverResponse;
        }
        List<OrderItem> orderItemList = serverResponse.getData();
        // 计算总价
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);
        // 生成订单
        Order order = this.assembleOrder(userId,shippingId,payment);
        if(order == null){
            return ServerResponse.createByErrorMessage("生成订单错误");
        }
        if(CollectionUtils.isEmpty(orderItemList)){
            return ServerResponse.createByErrorMessage("生成订单号为空");
        }
        for(OrderItem orderItem:orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        // mybatis 批量插入
        orderItemMapper.batchInsert(orderItemList);

        // 生成成功,减少我们产品的库存
        this.reduceProductStock(orderItemList);
        // 清空一下购物车
        this.clearCart(cartList);

        // 返回前端数据
        OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }
    /**
     * @Desc 取消订单
     * @Author LovingLiu
    */
    public ServerResponse  cancel(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("该用户此订单不存在");
        }
        if(order.getStatus() != Const.OrderPayStatusEnum.NO_PAY.getStatus()){
            return ServerResponse.createByErrorMessage("已付款,无法取消订单");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderPayStatusEnum.CANCELED.getStatus());
        int resultCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("取消成功");
        }
        return ServerResponse.createByErrorMessage("取消失败");
    }
    /**
     * @Desc
     * @Author LovingLiu
    */
    public ServerResponse getOrderCartProduct(Integer userId){
        OrderProductVo orderProductVo = new OrderProductVo();
        // 从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        ServerResponse serverResponse = this.getCartOrderItem(userId,cartList);
        if(!serverResponse.ifSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList =(List<OrderItem>) serverResponse.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem: orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(this.assembleOrderItemVo(orderItem));
        }
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return ServerResponse.createBySuccess(orderProductVo);
    }
    /**
     * @Desc 获取订单详情
     * @Author LovingLiu
    */
    public ServerResponse<OrderVo> getOrderDetail(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("用户不存在该订单");
        }

        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId,orderNo);
        OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * @Desc 获得订单列表带分页
     * @Author LovingLiu
    */
    public ServerResponse<PageInfo> getOrderList(Integer userId, Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = this.assembleOrderVoList(userId,orderList);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }


    //--------------------------管理端------------------------------
    /**
     * @Desc 管理员端查询订单列表
     * @Author LovingLiu
    */

    public ServerResponse<PageInfo> manageOrderList(Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = this.assembleOrderVoList(null,orderList);

        PageInfo pageInfo = new PageInfo(orderList);

        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
    /**
     * @Desc 管理员端查询具体订单信息
     * @Author LovingLiu
    */
    public ServerResponse<OrderVo> manageOrderDetail(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(null,orderNo);
        OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }
    /**
     * @Desc
     * @Author LovingLiu
    */
    public ServerResponse<PageInfo> manageOrderSearch(Long orderNo,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectListByOrderNo(orderNo);
        List<OrderVo> orderVoList = Lists.newArrayList();
        if(CollectionUtils.isEmpty(orderList)){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        PageInfo pageResult = new PageInfo(orderList);
        for (Order order: orderList) {
            List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(null,order.getOrderNo());
            OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        pageResult.setList(orderVoList);

        return ServerResponse.createBySuccess(pageResult);
    }
    /**
     * @Desc 发货
     * @Author LovingLiu
    */
    public ServerResponse<String> manageSendGoods(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if(order.getStatus() == Const.OrderPayStatusEnum.PAID.getStatus()){
            order.setStatus(Const.OrderPayStatusEnum.SHIPPED.getStatus());
            order.setSendTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            return ServerResponse.createBySuccessMessage("发货成功");
        }
        return ServerResponse.createBySuccessMessage("发货失败");
    }








    /**
     * @Desc 支付宝官方 简单应答
     * @Author LovingLiu
     */
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

    /**
     * @Desc 计算订单总价
     * @Author LovingLiu
    */
    private ServerResponse<List<OrderItem>> getCartOrderItem(Integer userId,List<Cart> cartList){
        List<OrderItem>  orderItemList = Lists.newArrayList();
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }

        // 校验购物车的数据,包括产品的状态和数量
        for(Cart cartItem: cartList){
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            // 在售状态
            if(Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+" 不是在售卖状态!");
            }
            // 校验库存
            if(cartItem.getQuantity() > product.getStock()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+" 库存不足!");
            }

            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }
    /**
     * @Desc 计算订单总价
     * @Author LovingLiu
    */
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem: orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    /**
     * @Desc 组装订单Order
     * @Author LovingLiu
    */

    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){
        Order order = new Order();
        order.setOrderNo(this.generateOrderNo());
        // 设置订单支付状态
        order.setStatus(Const.OrderPayStatusEnum.NO_PAY.getStatus());
        // 设置运费 以后扩展
        order.setPostage(0);
        // 支付类型
        order.setPaymentType(Const.PaymentType.ONLINE_PAY.getStatus());
        // 设置金额
        order.setPayment(payment);

        order.setUserId(userId);
        order.setShippingId(shippingId);
        // 发货时间

        int rowCount = orderMapper.insertSelective(order);
        if(rowCount > 0){
            return order;
        }
        return null;
    }
    /**
     * @Desc 生成订单号
     * @Author LovingLiu
    */
    private long generateOrderNo(){
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);// 生成0-100 之间的随机数,引文并发请求的时候 单纯使用时间戳 会使得订单号相同，必有一人下单失败
    }

    /**
     * @Desc 减少产品库存数量
     * @Author LovingLiu
    */
    private void reduceProductStock(List<OrderItem> orderItemList){
        for (OrderItem orderItem:orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    /**
     * @Desc 清空购物车
     * @Author LovingLiu
    */
    private void clearCart( List<Cart> cartList){
        for (Cart cart:cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    /**
     * @Desc 组装数据
     * @Author LovingLiu
    */
    private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentType.statusOf(order.getPaymentType()).getDesc());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());

        orderVo.setStatusDesc(Const.OrderPayStatusEnum.statusOf(order.getStatus()).getDesc());

        orderVo.setShippingId(order.getShippingId());

        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping != null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = this.assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }

        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    /**
     * @Desc 封装购物地址
     * @Author LovingLiu
    */
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        return  shippingVo;
    }
    /**
     * @Desc 封装OrderItemVO对象
     * @Author LovingLiu
    */
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));

        return orderItemVo;
    }

    private List<OrderVo> assembleOrderVoList(Integer userId, List<Order> orderList){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order: orderList) {
            List<OrderItem> orderItemList = Lists.newArrayList();
            if(userId == null){
                orderItemList = orderItemMapper.selectByUserIdAndOrderNo(null,order.getOrderNo());
            }else{
                orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId,order.getOrderNo());
            }
            OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

}
