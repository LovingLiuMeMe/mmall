package cn.lovingliu.service;

import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.vo.CartVo;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-09
 */
public interface ICartService {
    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count);
    ServerResponse<CartVo> deleteProduct(Integer userId,String productIds);
    ServerResponse<CartVo> selectList(Integer userId);
    ServerResponse<CartVo> selectOrUnselectProduct(Integer userId,Integer checked,Integer productId);
    ServerResponse<Integer> getCartProductCount(Integer userId);
}
