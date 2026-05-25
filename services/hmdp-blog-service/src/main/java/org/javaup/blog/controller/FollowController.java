package org.javaup.blog.controller;

import jakarta.annotation.Resource;
import org.javaup.blog.constant.BlogConstants;
import org.javaup.blog.service.FollowService;
import org.javaup.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private FollowService followService;

    @PutMapping("/{id}/{isFollow}")
    public Result<?> follow(@RequestHeader(value = BlogConstants.USER_ID_HEADER, required = false) Long currentUserId,
                            @PathVariable("id") Long followUserId,
                            @PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(currentUserId, followUserId, isFollow);
    }

    @GetMapping("/or/not/{id}")
    public Result<?> isFollow(@RequestHeader(value = BlogConstants.USER_ID_HEADER, required = false) Long currentUserId,
                              @PathVariable("id") Long followUserId) {
        return followService.isFollow(currentUserId, followUserId);
    }

    @GetMapping("/common/{id}")
    public Result<?> followCommons(@RequestHeader(value = BlogConstants.USER_ID_HEADER, required = false) Long currentUserId,
                                   @PathVariable("id") Long targetUserId) {
        return followService.followCommons(currentUserId, targetUserId);
    }
}
