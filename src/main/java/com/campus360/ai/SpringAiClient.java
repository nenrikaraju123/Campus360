package com.campus360.ai;

import org.springframework.ai.chat.client.ChatClient;

/** OpenAI-backed implementation using Spring AI's {@link ChatClient}. */
public class SpringAiClient implements AiClient {

    private final ChatClient chatClient;

    public SpringAiClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    @Override
    public boolean isLive() {
        return true;
    }
}
