package cn.lovingliu.service.impl;

import cn.lovingliu.common.ServerResponse;
import cn.lovingliu.dao.ShippingMapper;
import cn.lovingliu.pojo.Shipping;
import cn.lovingliu.service.IShippingService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-10
 */
@Service("shipService")
public class ShippingServiceImpl implements IShippingService {
    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse addUserShip(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if(rowCount > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功",result);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    public ServerResponse deleteUserShipping(Integer userId,Integer shippingId){
        // 横向越权的问题 deleteByPrimaryKey(shippingId) 传入一个非当前登陆用户的 shippingId 就会把别人的删除
        int resultCount = shippingMapper.deleteByShippingIdUserId(userId,shippingId);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("删除成功");
        }
        return ServerResponse.createByErrorMessage("删除失败");
    }

    public ServerResponse updateUserShipping(Integer userId, Shipping shipping){
        // 为什这里还要设置userId 还是防止恶意构造shipping 其中的userId 不是当前登陆者id 这样也会导致更新错误
        shipping.setUserId(userId);

        int rowCount = shippingMapper.updateByShipping(shipping);
        if(rowCount > 0){
            return ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    public ServerResponse<Shipping> selectUserShipping(Integer userId,Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId,shippingId);
        if(shipping == null){
            return ServerResponse.createByErrorMessage("无法查询到该地址");
        }
        return ServerResponse.createBySuccess("查询成功",shipping);
    }

    public ServerResponse<PageInfo> selectShippingList(Integer userId,Integer pageNum,int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> list = shippingMapper.selectListByUserId(userId);
        PageInfo<Shipping> pageInfo = new PageInfo<>(list);

        return ServerResponse.createBySuccess("查询成功",pageInfo);
    }
}
