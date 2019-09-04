package cn.lovingliu.service;

import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.pojo.User;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-04
 */
public interface IUserService {
    ServerResponse<User> login(String username, String password);
}
