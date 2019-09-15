package cn.lovingliu.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-04
 */
public class Const {
    public static final String CURRENT_USER = "current_user";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    // 定义了内部接口 存放属性
    public interface Role{
        int ROLE_CUSTOMER = 1; // 普通用户
        int ROLE_ADMIN = 0;// 管理员
    }
    // 设置ProductList的排序方式
    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }
    // 设置购物车中商品的选中状态
    public interface Cart{
        int CHECKED = 1; // 购物车选中状态
        int UN_CHECKED = 0; // 购物车中未选中状态
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }
    public enum ProductStatusEnum{
        ON_SALE(1,"在售");
        private String value;
        private int code;
        ProductStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
    public enum OrderPayStatusEnum{
        CANCELED(0,"订单已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已支付"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭");

        private int status;
        private String desc;
        OrderPayStatusEnum(int status, String desc){
            this.status = status;
            this.desc = desc;
        }

        public int getStatus() {
            return status;
        }

        public String getDesc() {
            return desc;
        }

        public static OrderPayStatusEnum statusOf(int status){
            for(OrderPayStatusEnum orderPayStatusEnum : values()){
                if(orderPayStatusEnum.getStatus() == status){
                    return orderPayStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }
    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝"),
        WECHAT_PAY(2,"微信");

        private int status;
        private String desc;
        PayPlatformEnum(int status, String desc){
            this.status = status;
            this.desc = desc;
        }

        public int getStatus() {
            return status;
        }

        public String getDesc() {
            return desc;
        }
    }
    public enum PaymentType{
        ONLINE_PAY(1,"在线支付");
        private int status;
        private String desc;
        PaymentType(int status, String desc){
            this.status = status;
            this.desc = desc;
        }

        public int getStatus() {
            return status;
        }

        public String getDesc() {
            return desc;
        }

        public static PaymentType statusOf(int status){
            for(PaymentType paymentType : values()){
                if(paymentType.getStatus() == status){
                    return paymentType;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }
    public interface  AlipayCallBack{ // 等待买家付款
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";

    }
}
