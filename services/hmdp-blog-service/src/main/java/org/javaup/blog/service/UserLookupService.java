package org.javaup.blog.service;

import org.javaup.dto.UserDTO;

import java.util.List;

/**
 * @description: 用户查询服务，封装跨服务的用户信息获取（底层调用 hmdp-user-service）
 * @maintainer: lrb
 **/
public interface UserLookupService {

    /**
     * 按 ID 查询单个用户，未找到时返回 null
     */
    UserDTO getUserById(Long id);

    /**
     * 批量查询用户，结果按入参 ids 顺序返回
     */
    List<UserDTO> listUserDTOsByIds(List<Long> ids);
}
