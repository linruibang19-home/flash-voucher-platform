package org.javaup.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.javaup.blog.entity.User;
import org.javaup.blog.mapper.UserMapper;
import org.javaup.blog.service.UserLookupService;
import org.javaup.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserLookupServiceImpl extends ServiceImpl<UserMapper, User> implements UserLookupService {

    @Override
    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setNickName(user.getNickName());
        userDTO.setIcon(user.getIcon());
        return userDTO;
    }

    @Override
    public List<UserDTO> listUserDTOsByIds(List<Long> ids) {
        return listByIds(ids).stream().map(this::toUserDTO).toList();
    }
}
