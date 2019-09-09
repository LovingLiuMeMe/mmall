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
}
