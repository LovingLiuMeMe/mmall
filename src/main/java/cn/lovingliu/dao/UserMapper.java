package cn.lovingliu.dao;

import cn.lovingliu.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String username);
    // 多参数查询的时候 必须要指定@Param
    User selectLogin(@Param("username") String username, @Param("password") String password);
}