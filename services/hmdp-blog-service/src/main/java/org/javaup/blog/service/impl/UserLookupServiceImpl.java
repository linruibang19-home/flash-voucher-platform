package org.javaup.blog.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.api.user.UserClient;
import org.javaup.blog.service.UserLookupService;
import org.javaup.dto.Result;
import org.javaup.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description: 用户查询服务实现，通过 Feign 调用 hmdp-user-service
 * @maintainer: lrb
 **/
@Slf4j
@Service
public class UserLookupServiceImpl implements UserLookupService {

    @Resource
    private UserClient userClient;

    @Override
    public UserDTO getUserById(Long id) {
        if (id == null) {
            return null;
        }
        try {
            Result<UserDTO> result = userClient.getUserById(id);
            return result != null ? result.getData() : null;
        } catch (Exception e) {
            log.warn("[UserLookup] getUserById 失败 id={}", id, e);
            return null;
        }
    }

    @Override
    public List<UserDTO> listUserDTOsByIds(List<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        try {
            Result<List<UserDTO>> result = userClient.getUserBatch(ids);
            if (result == null || result.getData() == null) {
                return Collections.emptyList();
            }
            // 按入参 ids 顺序返回
            Map<Long, UserDTO> map = result.getData().stream()
                    .filter(u -> u.getId() != null)
                    .collect(Collectors.toMap(UserDTO::getId, u -> u));
            return ids.stream().map(map::get).filter(Objects::nonNull).toList();
        } catch (Exception e) {
            log.warn("[UserLookup] listUserDTOsByIds 失败 ids={}", ids, e);
            return Collections.emptyList();
        }
    }
}
