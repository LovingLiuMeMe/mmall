package cn.lovingliu.service.impl;

import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.dao.CategoryMapper;
import cn.lovingliu.pojo.Category;
import cn.lovingliu.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-05
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

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
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess("获得成功",categoryList);
    }
    public ServerResponse getCategoryAndChildrenById(Integer categoryId){
        Set<Category> set = new HashSet<>();
        findChildCategory(set,categoryId);

        if(categoryId != null){
            List<Integer> idList = new ArrayList<>();
            for (Category categoryItem:set) {
                idList.add(categoryItem.getId());
            }
            return ServerResponse.createBySuccess("获取成功",idList);
        }
       return ServerResponse.createByErrorMessage("获取失败");
    }

    /**
     * @Desc 递归函数 因为要排重 所以要重写hashCode和 equals
     * @Author LovingLiu
    */
    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null){
            categorySet.add(category);
        }
        // 查找子节点 递归算法一定要有推出条件
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);// 注意:mybatis 当查询无记录的时候 并不会返回null 所以不用使用非null判断
        for(Category categoryItem : categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }

}
