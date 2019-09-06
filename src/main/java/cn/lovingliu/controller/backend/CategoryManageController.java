package cn.lovingliu.controller.backend;

import cn.lovingliu.common.Const;
import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.pojo.User;
import cn.lovingliu.service.ICategoryService;
import cn.lovingliu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @Author：LovingLiu
 * @Description: 分类管理
 * @Date：Created in 2019-09-05
 */
@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {
    @Autowired
    private IUserService userService;

    @Autowired
    private ICategoryService categoryService;
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session,String categoryName,@RequestParam(value = "parentId",defaultValue = "0") int parentId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录,请登陆");
        }
        // 校验是否是管理员
        if(!userService.checkAdminRole(user).ifSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员");
        }
        return categoryService.addCategory(categoryName,parentId);
    }
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session,Integer categoryId,String categoryName){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录,请登陆");
        }
        // 校验是否是管理员
        if(!userService.checkAdminRole(user).ifSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员");
        }
        return categoryService.updateCategoryName(categoryId,categoryName);
    }
    /**
     * @Desc 获取子节点平级的category信息
     * @Author LovingLiu
    */

    public ServerResponse getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录,请登陆");
        }
        // 校验是否是管理员
        if(!userService.checkAdminRole(user).ifSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员");
        }
        // 查询子节点的categoryx信息

        return null;
    }
}

