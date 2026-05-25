package org.javaup.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.javaup.entity.UserInfo;

/**
 * @description: 用户信息 接口
 * @maintainer: lrb
 **/
public interface IUserInfoService extends IService<UserInfo> {
    
    /**
     * 通过用户id查询用户信息
     * @param userId 用户ID
     * @return 结果
     */
    UserInfo getByUserId(Long userId);

}
