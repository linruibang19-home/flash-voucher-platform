package org.javaup.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "hmdp.gateway.auth")
public class GatewayAuthProperties {

    private boolean enabled = true;
    private String loginUserKeyPrefix = "login:token:";
    private Duration tokenTtl = Duration.ofMinutes(36000);
    private List<String> whitelist = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLoginUserKeyPrefix() {
        return loginUserKeyPrefix;
    }

    public void setLoginUserKeyPrefix(String loginUserKeyPrefix) {
        this.loginUserKeyPrefix = loginUserKeyPrefix;
    }

    public Duration getTokenTtl() {
        return tokenTtl;
    }

    public void setTokenTtl(Duration tokenTtl) {
        this.tokenTtl = tokenTtl;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }
}
