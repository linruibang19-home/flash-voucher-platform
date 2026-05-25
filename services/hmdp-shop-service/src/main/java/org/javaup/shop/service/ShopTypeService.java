package org.javaup.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.javaup.dto.Result;
import org.javaup.shop.entity.ShopType;

import java.util.List;

public interface ShopTypeService extends IService<ShopType> {

    Result<List<ShopType>> queryTypeList();
}
