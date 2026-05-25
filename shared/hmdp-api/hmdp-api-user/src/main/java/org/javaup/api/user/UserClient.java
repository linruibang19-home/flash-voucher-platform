package org.javaup.api.user;

import org.javaup.dto.Result;
import org.javaup.dto.UserDTO;
import org.javaup.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @description: hmdp-user-service 的 Feign 客户端契约
 *               消费方引入此模块后通过 @EnableFeignClients(basePackages = "org.javaup.api.user") 启用
 * @maintainer: lrb
 **/
@FeignClient(name = "hmdp-user-service", url = "${hmdp.user-service.url:http://localhost:8082}")
public interface UserClient {

    /**
     * 按 ID 查询用户基本信息（id / nickName / icon）
     */
    @GetMapping("/user/{id}")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);

    /**
     * 批量查询用户基本信息
     */
    @PostMapping("/user/batch")
    Result<List<UserDTO>> getUserBatch(@RequestBody List<Long> ids);

    /**
     * 按 ID 查询用户详情（含会员等级）
     */
    @GetMapping("/user/info/{id}")
    Result<UserInfoDTO> getUserInfoById(@PathVariable("id") Long id);
}
