package cn.lovingliu.service;

import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.pojo.Shipping;
import com.github.pagehelper.PageInfo;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-10
 */
public interface IShippingService {
    ServerResponse addUserShip(Integer userId, Shipping shipping);
    ServerResponse deleteUserShipping(Integer userId,Integer shippingId);
    ServerResponse updateUserShipping(Integer userId, Shipping shipping);
    ServerResponse<Shipping> selectUserShipping(Integer userId,Integer shippingId);
    ServerResponse<PageInfo> selectShippingList(Integer userId, Integer pageNum, int pageSize);
}
