package com.campus360.platform.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds campus360.jwt.* configuration. */
@ConfigurationProperties(prefix = "campus360.jwt")
public class JwtProperties {

    /** Base64 or raw secret, at least 256 bits, used to sign access tokens. */
    private String secret = "dev-only-insecure-secret-change-me-0123456789-0123456789-abcd";
    private long accessTokenMinutes = 15;
    private long refreshTokenDays = 7;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenMinutes() {
        return accessTokenMinutes;
    }

    public void setAccessTokenMinutes(long accessTokenMinutes) {
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public long getRefreshTokenDays() {
        return refreshTokenDays;
    }

    public void setRefreshTokenDays(long refreshTokenDays) {
        this.refreshTokenDays = refreshTokenDays;
    }
}
