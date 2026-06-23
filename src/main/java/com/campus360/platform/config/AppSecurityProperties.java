package com.campus360.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "campus360.security")
public class AppSecurityProperties {
    private boolean requireHttps = false;
    private int maxLoginAttempts = 5;

    public boolean isRequireHttps() {
        return requireHttps;
    }

    public void setRequireHttps(boolean requireHttps) {
        this.requireHttps = requireHttps;
    }

    public int getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public void setMaxLoginAttempts(int maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }
}
