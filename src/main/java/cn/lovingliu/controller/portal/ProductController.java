package cn.lovingliu.controller.portal;

import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.service.IProductService;
import cn.lovingliu.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author：LovingLiu
 * @Description: 前台的product
 * @Date：Created in 2019-09-08
 */
@Controller
@RequestMapping("/product/")
public class ProductController {
    @Autowired
    private IProductService productService;

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId){
        return productService.getProductDetail(productId);
    }
    /**
     * @Desc 模糊搜索商品列表 注意required 默认是必须传的，若为非必须 必须声明
     * @Author LovingLiu
    */

    public ServerResponse<PageInfo> list(
            @RequestParam(value = "keyword",required = false)String keyword,
            @RequestParam(value = "categoryId",required = false)Integer categoryId,
            @RequestParam(value = "pageNum",defaultValue = "1")int pageNum,
            @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,
            @RequestParam(value = "orderBy",defaultValue = "") String orderBy){
        return productService.getProductByKeywordCategory(keyword,categoryId,pageNum,pageSize,orderBy);
    }
}
