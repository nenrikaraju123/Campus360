package com.campus360.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chooses the {@link AiClient} implementation at startup: OpenAI via Spring AI
 * when enabled and a ChatClient is available, otherwise the offline fallback.
 * This lets the app boot and run end-to-end with no AI key configured.
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    @Bean
    public AiClient aiClient(AiProperties props, ObjectProvider<ChatClient.Builder> chatClientBuilder) {
        if (props.isEnabled()) {
            ChatClient.Builder builder = chatClientBuilder.getIfAvailable();
            if (builder != null) {
                return new SpringAiClient(builder.build());
            }
        }
        return new HeuristicAiClient();
    }
}
