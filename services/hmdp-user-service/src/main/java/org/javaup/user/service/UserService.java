package org.javaup.user.service;

import org.javaup.dto.Result;
import org.javaup.dto.UserDTO;
import org.javaup.dto.UserInfoDTO;

import java.util.List;

public interface UserService {

    Result<UserDTO> queryUserById(Long userId);

    Result<List<UserDTO>> queryUserBatch(List<Long> ids);

    Result<UserInfoDTO> queryUserInfoByUserId(Long userId);

    Result<Void> updateLevel(Long currentUserId, Integer newLevel);

    Result<Void> sign(Long currentUserId);

    Result<Integer> signCount(Long currentUserId);
}
