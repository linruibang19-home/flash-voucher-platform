package org.javaup.user.service;

import org.javaup.dto.Result;
import org.javaup.dto.UserDTO;
import org.javaup.user.entity.UserInfo;

public interface UserService {

    Result<UserDTO> queryUserById(Long userId);

    Result<UserInfo> queryUserInfoByUserId(Long userId);

    Result<Void> updateLevel(Long currentUserId, Integer newLevel);

    Result<Void> sign(Long currentUserId);

    Result<Integer> signCount(Long currentUserId);
}
