package org.javaup.auth.constant;

public final class AuthRedisConstants {

    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final long LOGIN_CODE_TTL_MINUTES = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final long LOGIN_USER_TTL_MINUTES = 36000L;
    public static final String USER_NICK_NAME_PREFIX = "user_";

    private AuthRedisConstants() {
    }
}
