package com.campus360.ai;

/**
 * Provider-agnostic text generation. Implemented by {@code SpringAiClient}
 * (OpenAI via Spring AI) when AI is enabled, and {@code HeuristicAiClient}
 * (deterministic, offline) otherwise. Keeping the rest of the codebase behind
 * this interface means the AI provider can change without touching business code.
 */
public interface AiClient {

    /**
     * @param systemPrompt role/instructions for the model
     * @param userPrompt   the concrete request
     * @return generated text
     */
    String complete(String systemPrompt, String userPrompt);

    /** Whether this client is backed by a live LLM (vs the offline fallback). */
    boolean isLive();
}
