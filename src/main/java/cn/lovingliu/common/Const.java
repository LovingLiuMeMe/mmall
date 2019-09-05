package cn.lovingliu.common;

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
        int ROLE_CUSTOMER = 0; // 普通用户
        int ROLE_ADMIN = 1;// 管理员
    }
}
