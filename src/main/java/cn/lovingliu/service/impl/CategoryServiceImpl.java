package cn.lovingliu.service.impl;

import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.dao.CategoryMapper;
import cn.lovingliu.pojo.Category;
import cn.lovingliu.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-05
 */
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName,Integer parentId){
        if(parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加品类错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true); // 可用

        int rowCount = categoryMapper.insert(category);
        if(rowCount > 0){
            return ServerResponse.createBySuccess("添加品类成功",category);
        }
        return  ServerResponse.createByErrorMessage("添加品类失败");
    }
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName){
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更新品类名称错误");
        }
        Category category = new Category();
        category.setId(categoryId);// 一定要set主键 因为选择性的更新也是通过主键更新的
        category.setName(categoryName);


        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount > 0){
            return ServerResponse.createBySuccess("更新品类名称成功",category);
        }
        return ServerResponse.createByErrorMessage("更新品类名称失败");
    }
}
