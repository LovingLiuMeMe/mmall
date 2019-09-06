package cn.lovingliu.service.impl;

import cn.lovingliu.common.Const;
import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.common.TokenCache;
import cn.lovingliu.dao.UserMapper;
import cn.lovingliu.pojo.User;
import cn.lovingliu.service.IUserService;
import cn.lovingliu.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5Password);
        if(user==null){
            return ServerResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功",user);
    }
    /**
     * @Desc 用户注册
     * @Author LovingLiu
    */

    public ServerResponse<String> register(User user) {
        ServerResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.ifSuccess()){
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.ifSuccess()){
            return validResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        // MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccess("注册成功");
    }
    /**
     * @Desc 注册校验 防止有人直接注册
     * @Author LovingLiu
    */
    public ServerResponse<String> checkValid(String str,String type){
        if(StringUtils.isNotBlank(type)){
            // 开始校验
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if(resultCount>0){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkUserEmail(str);
                if(resultCount>0){
                    return ServerResponse.createByErrorMessage("该邮箱已经被使用了");
                }
            }
        }else{
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }
    /**
     * @Desc 查询忘记密码的参数
     * @Author LovingLiu
    */

    public ServerResponse<String> selectQuestion(String username){
        ServerResponse<String> validResponse = checkValid(username,Const.USERNAME);
        if(validResponse.ifSuccess()){
            // 用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess("获取忘记问题成功",question);
        }
        return ServerResponse.createByErrorMessage("找回密码问题未设置");
    }
    /**
     * @Desc 检测答案
     * @Author LovingLiu
    */
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount>0){
            // 说名回答正确
            String token = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username,token);
            return ServerResponse.createBySuccess(token);
        }
        return ServerResponse.createByErrorMessage("回答错误");
    }
    /**
     * @Desc 回答问题重置密码
     * @Author LovingLiu
    */
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，需要传递forgetToken");
        }
        // 校验username的正确性 已免token_ 任然有拿到数据的危险
        ServerResponse<String> validResponse = checkValid(username,Const.USERNAME);
        if(validResponse.ifSuccess()){
            // 用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }
        if(StringUtils.equals(forgetToken,token)){
            // StringUtils.equals(null, "abc")  = false
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);
            if(rowCount > 0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }else{
            return ServerResponse.createBySuccessMessage("token错误,请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改失败");
    }
    /**
     * @Desc 重置密码（登陆状态下）
     * @Author LovingLiu
    */

    public ServerResponse<String> restPassword(String passwordOld,String passwordNew,User user){
        // 防止横向🈷️权 要校验一下旧密码，一定要指定是这个用户，因为我们会查询一个count(1),如果不指定Id，那么结果就是true
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }
    /**
     * @Desc 更新用户信息
     * @Author LovingLiu
    */

    public ServerResponse<User> updateInformation(User user){
        // username 是不能被更新的
        // email 校验 新的email 是否存在，存在的话是否是当前登陆用户
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("email 已经存在,请更换email再尝试");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新失败");
    }
    /**
     * @Desc 获得详细信息
     * @Author LovingLiu
    */

    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("获取成功",user);
    }

    // backend
    /**
     * @Desc 校验是否是管理员
     * @Author LovingLiu
    */

    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole().equals(Const.Role.ROLE_ADMIN)){
             return ServerResponse.createBySuccess();
        }else {
            return ServerResponse.createByError();
        }
    }


}
