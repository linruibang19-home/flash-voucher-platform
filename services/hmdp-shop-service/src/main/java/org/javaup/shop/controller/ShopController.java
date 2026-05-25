package org.javaup.shop.controller;

import jakarta.annotation.Resource;
import org.javaup.dto.Result;
import org.javaup.shop.entity.Shop;
import org.javaup.shop.service.ShopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    private ShopService shopService;

    @GetMapping("/{id}")
    public Result<Shop> queryShopById(@PathVariable("id") Long id) {
        return shopService.queryById(id);
    }

    @PostMapping
    public Result<Long> saveShop(@RequestBody Shop shop) {
        return shopService.saveShop(shop);
    }

    @PutMapping
    public Result<Void> updateShop(@RequestBody Shop shop) {
        return shopService.updateShop(shop);
    }

    @GetMapping("/of/type")
    public Result<?> queryShopByType(@RequestParam("typeId") Integer typeId,
                                     @RequestParam(value = "current", defaultValue = "1") Integer current,
                                     @RequestParam(value = "x", required = false) Double x,
                                     @RequestParam(value = "y", required = false) Double y) {
        return shopService.queryShopByType(typeId, current, x, y);
    }

    @GetMapping("/of/name")
    public Result<?> queryShopByName(@RequestParam(value = "name", required = false) String name,
                                     @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return shopService.queryShopByName(name, current);
    }
}
