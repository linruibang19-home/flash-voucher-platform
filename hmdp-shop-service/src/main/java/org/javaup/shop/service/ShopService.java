package org.javaup.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.javaup.dto.Result;
import org.javaup.shop.entity.Shop;

public interface ShopService extends IService<Shop> {

    Result<Long> saveShop(Shop shop);

    Result<Shop> queryById(Long id);

    Result<Void> updateShop(Shop shop);

    Result<?> queryShopByType(Integer typeId, Integer current, Double x, Double y);

    Result<?> queryShopByName(String name, Integer current);
}
