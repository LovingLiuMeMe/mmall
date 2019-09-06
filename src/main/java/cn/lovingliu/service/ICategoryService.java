package cn.lovingliu.service;

import cn.lovingliu.common.ServerResponse;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-05
 */
public interface ICategoryService {
    ServerResponse addCategory(String categoryName, Integer parentId);
    ServerResponse updateCategoryName(Integer categoryId, String categoryName);
}
