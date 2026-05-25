package org.javaup.shop.controller;

import jakarta.annotation.Resource;
import org.javaup.dto.Result;
import org.javaup.shop.entity.ShopType;
import org.javaup.shop.service.ShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Resource
    private ShopTypeService shopTypeService;

    @GetMapping("/list")
    public Result<List<ShopType>> queryTypeList() {
        return shopTypeService.queryTypeList();
    }
}
