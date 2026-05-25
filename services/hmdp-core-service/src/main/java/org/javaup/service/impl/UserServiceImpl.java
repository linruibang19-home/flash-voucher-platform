package org.javaup.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.javaup.entity.User;
import org.javaup.mapper.UserMapper;
import org.javaup.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * @description: User query service retained for monolith-local blog/follow lookups.
 * @maintainer: lrb
 **/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
}
