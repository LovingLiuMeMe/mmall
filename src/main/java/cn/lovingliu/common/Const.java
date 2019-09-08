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
    public enum ProductStatusEnum{
        ON_SALE(0,"上线");
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
}
