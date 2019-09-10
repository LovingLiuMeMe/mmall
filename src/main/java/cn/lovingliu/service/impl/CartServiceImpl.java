package cn.lovingliu.service.impl;

import cn.lovingliu.common.Const;
import cn.lovingliu.common.ResponseCode;
import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.dao.CartMapper;
import cn.lovingliu.dao.ProductMapper;
import cn.lovingliu.pojo.Cart;
import cn.lovingliu.pojo.Product;
import cn.lovingliu.service.ICartService;
import cn.lovingliu.util.BigDecimalUtil;
import cn.lovingliu.util.PropertiesUtil;
import cn.lovingliu.vo.CartProductVo;
import cn.lovingliu.vo.CartVo;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-09
 */
@Service("cartService")
public class CartServiceImpl implements ICartService {
    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count){
        if(productId == null || count == null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);// 获得具体商品的购物车内容
        if(cart == null){
            // 这个产品不在当前登录用户 的购物车中，需要新增一条激励
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);// 设置假如购物车的数量
            cartItem.setChecked(Const.Cart.CHECKED);// 默认选中
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        }else{
            // 这个产品已经在购物车里
            // 数量相加

            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            // 更新数据库内容
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.selectList(userId);
    }

    public ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count){
        if(productId == null || count == null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);

        if(cart != null){
            // 更新指定用户的购物车中的产品数量
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        return this.selectList(userId);
    }

    public ServerResponse<CartVo> deleteProduct(Integer userId,String productIds){
        List<String> productIdList = Splitter.on(",").splitToList(productIds);// 1,2,3,4,5
        if(CollectionUtils.isEmpty(productIdList)){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId,productIdList);
        return this.selectList(userId);
    }

    public ServerResponse<CartVo> selectList(Integer userId){
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess("获取成功",cartVo);
    }

    public ServerResponse<CartVo> selectOrUnselectProduct(Integer userId,Integer checked,Integer productId){
        cartMapper.checkedOrUncheckedAllProduct(userId,checked,productId);
        return this.selectList(userId);
    }

    public ServerResponse<Integer> getCartProductCount(Integer userId){
        if(userId == null){
            // 未登陆返回一个0
            return ServerResponse.createBySuccess("查询成功",0);
        }
        int count = cartMapper.selectCartProductCountByUserId(userId);
        return ServerResponse.createBySuccess("获取成功",count);
    }

    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        // 查询数据库中该用户的所有的购物车内容
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        // 创建数据库中product对应的productVo 数据库mmall_cart中 只存在product_id无法在前端展示
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");
        if(CollectionUtils.isNotEmpty(cartList)){
            for(Cart cartItem : cartList){
                CartProductVo  cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if(product != null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    int buyLimitCount = 0;
                    if(product.getStock() >= cartItem.getQuantity()){
                        // 产品的总库存大于 购物车的数量
                        buyLimitCount = cartItem.getQuantity(); // 设置为要买的数量
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else{
                        // 产品的总库存小于 购物车的数量
                        buyLimitCount = product.getStock(); // 直接将剩余库存 设置为购物车中的数量
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        // 购物车中更新有效库存
                        Cart cartForQuantity = new Cart();
                        // 在上面 count = cart.getQuantity() + count 进行一个纠正
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);// 设置为产品库存
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);

                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    // 计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));// 单价 * 数量 doubleValue() 将BigDecimal => double
                    cartProductVo.setProductChecked(cartItem.getChecked());

                }

                if(cartItem.getChecked() == Const.Cart.CHECKED){
                    // 如果是勾选的 增加到购物车的总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());// doubleValue() 将Bigdecimal 转换成 double
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
    }

}
