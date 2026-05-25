package org.javaup.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.javaup.blog.entity.User;
import org.javaup.dto.UserDTO;

import java.util.List;

public interface UserLookupService extends IService<User> {

    UserDTO toUserDTO(User user);

    List<UserDTO> listUserDTOsByIds(List<Long> ids);
}
