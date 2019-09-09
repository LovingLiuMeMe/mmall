package cn.lovingliu.controller.backend;

import cn.lovingliu.common.Const;
import cn.lovingliu.common.ResponseCode;
import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.pojo.Product;
import cn.lovingliu.pojo.User;
import cn.lovingliu.service.IFileService;
import cn.lovingliu.service.IProductService;
import cn.lovingliu.service.IUserService;
import cn.lovingliu.util.PropertiesUtil;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-06
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {
    @Autowired
    private IUserService userService;

    @Autowired
    private IProductService productService;

    @Autowired
    private IFileService fileService;
    /**
     * @Desc
     * @Author LovingLiu
    */

    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,请登录管理员");
        }
        if(!userService.checkAdminRole(user).ifSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作");
        }
        return productService.saveOrUpdateProduct(product);
    }
    /**
     * @Desc 设置商品上下架
     * @Author LovingLiu
    */

    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, @RequestParam(value = "status",defaultValue = "1")Integer status){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,请登录管理员");
        }
        if(!userService.checkAdminRole(user).ifSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作");
        }

        return productService.setStatus(productId,status);
    }
    /**
     * @Desc 获得商品详情
     * @Author LovingLiu
    */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,请登录管理员");
        }
        if(!userService.checkAdminRole(user).ifSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作");
        }
        return productService.manageProductDetail(productId);
    }
    /**
     * @Desc 使用Mybatis的page-helper分页插件分页 pageNum当前页面 pageSize每页的容量
     * @Author LovingLiu
    */

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,请登录管理员");
        }
        if(!userService.checkAdminRole(user).ifSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作");
        }
        return productService.getProductList(pageNum, pageSize);
    }
    /**
     * @Desc 商品的模糊查询
     * @Author LovingLiu
    */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session, String productName, Integer productId, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,请登录管理员");
        }
        if(!userService.checkAdminRole(user).ifSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作");
        }
        return productService.searchProduct(productName,productId,pageNum,pageSize);
    }

    /**
     * @Desc 商品的图片的上传
     * @Author LovingLiu
    */
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session,@RequestParam(name = "upload_file",required = false) MultipartFile file, HttpServletRequest request){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,请登录管理员");
        }
        if(!userService.checkAdminRole(user).ifSuccess()){
            return ServerResponse.createByErrorMessage("无权限操作");
        }

        String path = request.getSession().getServletContext().getRealPath("upload");// 拿到的是和WEB-INF同级的（服务器的发布环境）
        String targetFileName = fileService.upload(file,path);
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
        Map fileMap = Maps.newHashMap();
        fileMap.put("uri",targetFileName);
        fileMap.put("url",url);

        return ServerResponse.createBySuccess("上传成功",fileMap);
    }

    /**
     * @Desc
     * @Author LovingLiu
    */

    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, HttpServletResponse response, @RequestParam(name = "upload_file",required = false) MultipartFile file, HttpServletRequest request){
        Map resultMap = Maps.newHashMap();
        resultMap.put("success",false);
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            resultMap.put("msg","未登录,请登录管理员");
            return resultMap;
        }
        if(!userService.checkAdminRole(user).ifSuccess()){
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
        // 富文本中对于返回值有自己的要求，我们使用的是simditor所以按照simditor
        String path = request.getSession().getServletContext().getRealPath("upload");// 拿到的是和WEB-INF同级的（服务器的发布环境）
        String targetFileName = fileService.upload(file,path);
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;

        resultMap.put("success",true);// 覆盖
        resultMap.put("msg","上传成功");// 覆盖
        resultMap.put("file_path",url);// 设置图片地址

        response.addHeader("Access-Control-Allow-Headers","X-File-Name");
        return resultMap;
    }

}
