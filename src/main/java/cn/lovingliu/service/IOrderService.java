package cn.lovingliu.service;

import cn.lovingliu.common.ServerResponse;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-12
 */
public interface IOrderService {
    ServerResponse orderPay(Long orderNo, Integer userId, String path);
}
