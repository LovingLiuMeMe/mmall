package cn.lovingliu.controller.portal;

import cn.lovingliu.common.Const;
import cn.lovingliu.common.ResponseCode;
import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.pojo.User;
import cn.lovingliu.service.ICartService;
import cn.lovingliu.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @Author：LovingLiu
 * @Description: 购物车模块
 * @Date：Created in 2019-09-09
 */
@Controller
@RequestMapping("/cart/")
public class CartController {
    @Autowired
    private ICartService cartService;
    /**
     * @Desc 在商品详情点击加入购物车
     * @Author LovingLiu
    */

    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse<CartVo> add(HttpSession session, Integer count, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return cartService.add(user.getId(),productId,count);
    }
    /**
     * @Desc 在购物车列表对购物车实现更新
     * @Author LovingLiu
    */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse<CartVo> update(HttpSession session, Integer count, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return cartService.update(user.getId(),productId,count);
    }
    /**
     * @Desc 删除购物车中指定商品
     * @Author LovingLiu
    */
    @RequestMapping("delete_product.do")
    @ResponseBody
    public ServerResponse<CartVo> deleteProduct(HttpSession session, String productIds){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return cartService.deleteProduct(user.getId(),productIds);
    }
    /**
     * @Desc 获取列表
     * @Author LovingLiu
    */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<CartVo> list(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return cartService.selectList(user.getId());
    }
    /**
     * @Desc 全选
     * @Author LovingLiu
    */
    @RequestMapping("select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return cartService.selectOrUnselectProduct(user.getId(),Const.Cart.CHECKED,null);
    }
    /**
     * @Desc 全反选
     * @Author LovingLiu
    */
    @RequestMapping("un_select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelectAll(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return cartService.selectOrUnselectProduct(user.getId(),Const.Cart.UN_CHECKED,null);
    }
    /**
     * @Desc 单独选
     * @Author LovingLiu
    */
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<CartVo> select(HttpSession session,Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return cartService.selectOrUnselectProduct(user.getId(),Const.Cart.CHECKED,productId);
    }
    /**
     * @Desc 单独反选
     * @Author LovingLiu
    */
    @RequestMapping("un_select.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelect(HttpSession session,Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return cartService.selectOrUnselectProduct(user.getId(),Const.Cart.UN_CHECKED,productId);
    }
    /**
     * @Desc 查询当前购物车的有效数量 右上角需要哦
     * @Author LovingLiu
    */
    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            // 未登陆返回一个0
            return ServerResponse.createBySuccess("查询成功",0);
        }
        return cartService.getCartProductCount(user.getId());
    }
}
