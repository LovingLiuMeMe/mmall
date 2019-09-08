package cn.lovingliu.service;

import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.pojo.Product;
import cn.lovingliu.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-06
 */
public interface IProductService {
    ServerResponse saveOrUpdateProduct(Product product);
    ServerResponse<String> setStatus(Integer productId);
    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductList(int pageNum, int pageSize);
    ServerResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize);
    ServerResponse<ProductDetailVo>  getProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy);
}
