package org.javaup.shop.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.javaup.dto.Result;
import org.javaup.shop.constant.ShopConstants;
import org.javaup.shop.entity.Shop;
import org.javaup.shop.mapper.ShopMapper;
import org.javaup.shop.service.ShopService;
import org.javaup.toolkit.SnowflakeIdGenerator;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public Result<Long> saveShop(Shop shop) {
        shop.setId(snowflakeIdGenerator.nextId());
        save(shop);
        return Result.ok(shop.getId());
    }

    @Override
    public Result<Shop> queryById(Long id) {
        Shop shop = queryShopWithCache(id);
        if (shop == null) {
            return Result.fail("Shop does not exist");
        }
        return Result.ok(shop);
    }

    private Shop queryShopWithCache(Long id) {
        String shopKey = ShopConstants.CACHE_SHOP_KEY + id;
        String cachedShop = stringRedisTemplate.opsForValue().get(shopKey);
        if (StringUtils.hasText(cachedShop)) {
            return readShop(cachedShop);
        }

        String nullKey = ShopConstants.CACHE_SHOP_NULL_KEY + id;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(nullKey))) {
            return null;
        }

        Shop shop = getById(id);
        if (shop == null) {
            stringRedisTemplate.opsForValue()
                    .set(nullKey, "1", ShopConstants.CACHE_NULL_TTL_MINUTES, TimeUnit.MINUTES);
            return null;
        }

        stringRedisTemplate.opsForValue()
                .set(shopKey, writeShop(shop), ShopConstants.CACHE_SHOP_TTL_MINUTES, TimeUnit.MINUTES);
        return shop;
    }

    @Override
    @Transactional
    public Result<Void> updateShop(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("Shop id must not be null");
        }
        updateById(shop);
        stringRedisTemplate.delete(List.of(
                ShopConstants.CACHE_SHOP_KEY + id,
                ShopConstants.CACHE_SHOP_NULL_KEY + id
        ));
        return Result.ok();
    }

    @Override
    public Result<?> queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        if (x == null || y == null) {
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, ShopConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page.getRecords());
        }

        int from = (current - 1) * ShopConstants.DEFAULT_PAGE_SIZE;
        int end = current * ShopConstants.DEFAULT_PAGE_SIZE;
        String key = ShopConstants.SHOP_GEO_KEY + typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .search(
                        key,
                        GeoReference.fromCoordinate(x, y),
                        new Distance(5000),
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
                );
        if (results == null) {
            return Result.ok(Collections.emptyList());
        }

        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> locations = results.getContent();
        if (locations.size() <= from) {
            return Result.ok(Collections.emptyList());
        }

        List<Long> ids = new ArrayList<>(locations.size());
        Map<String, Distance> distanceMap = new HashMap<>(locations.size());
        locations.stream().skip(from).forEach(result -> {
            String shopId = result.getContent().getName();
            ids.add(Long.valueOf(shopId));
            distanceMap.put(shopId, result.getDistance());
        });

        String idOrder = StringUtils.collectionToCommaDelimitedString(ids);
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD(id," + idOrder + ")").list();
        for (Shop shop : shops) {
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        return Result.ok(shops);
    }

    @Override
    public Result<?> queryShopByName(String name, Integer current) {
        Page<Shop> page = query()
                .like(StringUtils.hasText(name), "name", name)
                .page(new Page<>(current, ShopConstants.MAX_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }

    private Shop readShop(String value) {
        try {
            return objectMapper.readValue(value, Shop.class);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String writeShop(Shop shop) {
        try {
            return objectMapper.writeValueAsString(shop);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
