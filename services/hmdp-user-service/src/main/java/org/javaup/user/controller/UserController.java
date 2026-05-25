package org.javaup.user.controller;

import jakarta.annotation.Resource;
import org.javaup.dto.Result;
import org.javaup.dto.UserDTO;
import org.javaup.dto.UserInfoDTO;
import org.javaup.user.constant.UserHeaders;
import org.javaup.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/info/{id}")
    public Result<UserInfoDTO> info(@PathVariable("id") Long userId) {
        return userService.queryUserInfoByUserId(userId);
    }

    @PostMapping("/batch")
    public Result<List<UserDTO>> getUserBatch(@RequestBody List<Long> ids) {
        return userService.queryUserBatch(ids);
    }

    @PostMapping("/level/update")
    public Result<Void> updateLevel(@RequestHeader(value = UserHeaders.USER_ID, required = false) Long currentUserId,
                                    @RequestParam("newLevel") Integer newLevel) {
        return userService.updateLevel(currentUserId, newLevel);
    }

    @GetMapping("/{id}")
    public Result<UserDTO> queryUserById(@PathVariable("id") Long userId) {
        return userService.queryUserById(userId);
    }

    @PostMapping("/sign")
    public Result<Void> sign(@RequestHeader(value = UserHeaders.USER_ID, required = false) Long currentUserId) {
        return userService.sign(currentUserId);
    }

    @GetMapping("/sign/count")
    public Result<Integer> signCount(@RequestHeader(value = UserHeaders.USER_ID, required = false) Long currentUserId) {
        return userService.signCount(currentUserId);
    }
}
