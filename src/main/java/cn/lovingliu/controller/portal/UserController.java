package cn.lovingliu.controller.portal;

import cn.lovingliu.common.Const;
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
}
