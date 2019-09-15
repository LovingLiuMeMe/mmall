package cn.lovingliu.service;

import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.vo.OrderVo;
import com.github.pagehelper.PageInfo;

import java.util.Map;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-12
 */
public interface IOrderService {
    ServerResponse orderPay(Long orderNo, Integer userId, String path);
    ServerResponse aliCallBack(Map<String,String> params);
    ServerResponse queryOrderPayStatus(Integer userId,Long orderId);
    ServerResponse createOrder(Integer userId,Integer shippingId);
    ServerResponse cancel(Integer userId,Long orderNo);
    ServerResponse getOrderCartProduct(Integer userId);
    ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo);
    ServerResponse<PageInfo> getOrderList(Integer userId, Integer pageNum, Integer pageSize);

    // backend
    ServerResponse<PageInfo> manageOrderList(Integer pageNum,Integer pageSize);
    ServerResponse<OrderVo> manageOrderDetail(Long orderNo);
    ServerResponse<PageInfo> manageOrderSearch(Long orderNo,Integer pageNum,Integer pageSize);
    ServerResponse<String> manageSendGoods(Long orderNo);
}
