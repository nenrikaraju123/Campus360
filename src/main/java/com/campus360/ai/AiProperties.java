package com.campus360.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "campus360.ai")
public class AiProperties {

    /** When false, use the deterministic offline fallback (no OpenAI key needed). */
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
