package org.javaup.order.utils;

import org.javaup.dto.UserDTO;

/**
 * @description: 用户上下文持有器（从 Gateway 注入的 X-User-Id 请求头）
 * @maintainer: lrb
 **/
public class UserHolder {

    private static final ThreadLocal<UserDTO> TL = new ThreadLocal<>();

    public static void saveUser(UserDTO user) {
        TL.set(user);
    }

    public static UserDTO getUser() {
        return TL.get();
    }

    public static void removeUser() {
        TL.remove();
    }
}
