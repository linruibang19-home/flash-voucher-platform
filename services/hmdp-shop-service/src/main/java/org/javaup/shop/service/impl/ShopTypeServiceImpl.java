package org.javaup.shop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.javaup.dto.Result;
import org.javaup.shop.entity.ShopType;
import org.javaup.shop.mapper.ShopTypeMapper;
import org.javaup.shop.service.ShopTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements ShopTypeService {

    @Override
    public Result<List<ShopType>> queryTypeList() {
        List<ShopType> typeList = query().orderByAsc("sort").list();
        return Result.ok(typeList);
    }
}
