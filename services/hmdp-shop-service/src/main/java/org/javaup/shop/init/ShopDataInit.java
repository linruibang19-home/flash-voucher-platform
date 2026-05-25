package org.javaup.shop.init;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.handler.BloomFilterHandlerFactory;
import org.javaup.shop.constant.ShopConstants;
import org.javaup.shop.entity.Shop;
import org.javaup.shop.service.ShopService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ShopDataInit {

    @Resource
    private ShopService shopService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private BloomFilterHandlerFactory bloomFilterHandlerFactory;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        List<Shop> shops = shopService.list();
        if (shops.isEmpty()) {
            log.info("Shop data init skipped because no shops were found");
            return;
        }

        clearShopCache(shops);
        rebuildShopBloomFilter(shops);
        rebuildShopGeo(shops);
        log.info("Shop data init completed, shop count: {}", shops.size());
    }

    private void clearShopCache(List<Shop> shops) {
        List<String> keys = shops.stream()
                .flatMap(shop -> List.of(
                        ShopConstants.CACHE_SHOP_KEY + shop.getId(),
                        ShopConstants.CACHE_SHOP_NULL_KEY + shop.getId()
                ).stream())
                .toList();
        stringRedisTemplate.delete(keys);
    }

    private void rebuildShopBloomFilter(List<Shop> shops) {
        shops.forEach(shop -> bloomFilterHandlerFactory
                .get(ShopConstants.SHOP_BLOOM_FILTER)
                .add(String.valueOf(shop.getId())));
    }

    private void rebuildShopGeo(List<Shop> shops) {
        Map<Long, List<Shop>> shopsByType = shops.stream()
                .filter(shop -> shop.getTypeId() != null && shop.getX() != null && shop.getY() != null)
                .collect(Collectors.groupingBy(Shop::getTypeId));

        shopsByType.forEach((typeId, typeShops) -> {
            String key = ShopConstants.SHOP_GEO_KEY + typeId;
            stringRedisTemplate.delete(key);
            typeShops.forEach(shop -> stringRedisTemplate.opsForGeo()
                    .add(key, new Point(shop.getX(), shop.getY()), shop.getId().toString()));
        });
    }
}
