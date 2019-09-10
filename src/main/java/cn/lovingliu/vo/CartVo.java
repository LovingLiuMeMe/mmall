package cn.lovingliu.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author：LovingLiu
 * @Description: 购物车VO
 * @Date：Created in 2019-09-09
 */
public class CartVo {
    private List<CartProductVo> cartProductVoList; // 购物车中商品列表
    private BigDecimal cartTotalPrice;// 购物车总金额
    private Boolean allChecked;// 是否已经都勾选
    private String imageHost; // 图片的前缀

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
