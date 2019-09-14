package cn.lovingliu.controller.portal;

import cn.lovingliu.common.Const;
import cn.lovingliu.common.ResponseCode;
import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.pojo.User;
import cn.lovingliu.service.IOrderService;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
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

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-12
 */
@Controller
@RequestMapping("/order/")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    private static  final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        // 注意在 upload 后面是没有斜线的
        String path = request.getSession().getServletContext().getRealPath("upload");
        return orderService.orderPay(orderNo,user.getId(),path);
    }

    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        Map<String,String> params = Maps.newHashMap();

        Map<String,String[]> requestParams = request.getParameterMap();
        Iterator<String> iter = requestParams.keySet().iterator();
        while(iter.hasNext()){
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for(int i = 0;i<values.length;i++){
                valueStr = (i == values.length-1)?valueStr + values[i]:valueStr + values[i]+",";
            }
            params.put(name, valueStr);
        }
        logger.info("支付宝回调,sian:{},trade_status:{},全部参数:{}",params.get("sian"),params.get("trade_status"),params.toString());

        // 验证回调的正确性是不是支付宝发的,并且避免重复通知
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType()); //调用SDK验证签名

        }catch (Exception e){
            logger.error("支付宝验证签名异常",e);
        }
        //todo 验证各种数据
        if(!signVerified){
            // 验证签名错误
            return ServerResponse.createByErrorMessage("非法请求");
        }

        ServerResponse serverResponse = orderService.aliCallBack(params);
        if(serverResponse.ifSuccess()){
            return Const.AlipayCallBack.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallBack.RESPONSE_FAILED;
    }

    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse serverResponse = orderService.queryOrderPayStatus(user.getId(),orderNo);
        if(serverResponse.ifSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }
}
