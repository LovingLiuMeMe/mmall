package cn.lovingliu.service.impl;

import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.dao.UserMapper;
import cn.lovingliu.pojo.User;
import cn.lovingliu.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-04
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        // 密码登陆md5
        User user = userMapper.selectLogin(username,password);
        if(user==null){
            return ServerResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功",user);
    }
}
