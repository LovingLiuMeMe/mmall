package cn.lovingliu.controller.backend;

import cn.lovingliu.common.Const;
import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.pojo.User;
import cn.lovingliu.service.IOrderService;
import cn.lovingliu.service.IUserService;
import cn.lovingliu.vo.OrderVo;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @Author：LovingLiu
 * @Description: 管理员端的订单模块
 * @Date：Created in 2019-09-14
 */
@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IUserService userService;
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderList(HttpSession session,
                                              @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                              @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录,请登陆");
        }
        // 校验是否是管理员
        if(!userService.checkAdminRole(user).ifSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员");
        }

        return orderService.manageOrderList(pageNum,pageSize);
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> detail(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录,请登陆");
        }
        // 校验是否是管理员
        if(!userService.checkAdminRole(user).ifSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员");
        }
        return orderService.manageOrderDetail(orderNo);
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> search(HttpSession session, Long orderNo,
                                          @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                          @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录,请登陆");
        }
        // 校验是否是管理员
        if(!userService.checkAdminRole(user).ifSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员");
        }
        return orderService.manageOrderSearch(orderNo,pageNum,pageSize);
    }
    /**
     * @Desc 发货
     * @Author LovingLiu
    */
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<String> orderSendGoods(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录,请登陆");
        }
        // 校验是否是管理员
        if(!userService.checkAdminRole(user).ifSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员");
        }
        return orderService.manageSendGoods(orderNo);
    }
}
