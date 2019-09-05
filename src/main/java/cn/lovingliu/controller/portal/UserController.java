package cn.lovingliu.controller.portal;

import cn.lovingliu.common.Const;
import cn.lovingliu.common.ResponseCode;
import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.pojo.User;
import cn.lovingliu.service.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @Author：LovingLiu
 * @Description: UserController
 * @Date：Created in 2019-09-04
 */
@Controller
@RequestMapping("/user/")
public class UserController {
    @Resource
    private IUserService userService;
    /**
     * 用户登陆
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> response = userService.login(username,password);
        if(response.ifSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> logout(HttpSession session){
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){
        return userService.register(user);
    }
    /**
     * @Desc 校验
     * @Author LovingLiu
    */

    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type){
        return userService.checkValid(str,type);
    }
    /**
     * @Desc 获取登陆用户的信息
     * @Author LovingLiu
    */
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null){
            return ServerResponse.createBySuccess("获取成功",user);
        }
        return ServerResponse.createByErrorMessage("用户未登陆,无法获取用户登陆信息");
    }
    /**
     * @Desc 用户忘记密码
     * @Author LovingLiu
     */
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return userService.selectQuestion(username);
    }
    /**
     * @Desc 检查回答是否正确
     * @Author LovingLiu
    */
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return userService.checkAnswer(username, question, answer);
    }

    /**
     * @Desc 回答正确重置密码
     * @Author LovingLiu
    */
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken){
        return userService.forgetResetPassword(username,passwordNew,forgetToken);
    }
    /**
     * @Desc 登陆状态下的重置密码
     * @Author LovingLiu
    */
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> restPassword(HttpSession session,String passwordOld,String passwordNew){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登陆");
        }
        return userService.restPassword(passwordOld,passwordNew,user);
    }

    /**
     * @Desc 更新个人消息
     * @Author LovingLiu
    */
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInformation(User user,HttpSession session){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("用户未登陆");
        }
        user.setId(currentUser.getId());// 前端传递过来的信息中是没有id的
        // username是不能更改的
        user.setUsername(currentUser.getUsername());

        ServerResponse response = userService.updateInformation(user);
        if(response.ifSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }
    /**
     * @Desc 获取个人信息并提示强制登陆 前端跳转登陆页
     * @Author LovingLiu
    */
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpSession session){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,需要强制登录");
        }
        return userService.getInformation(currentUser.getId());
    }

}
