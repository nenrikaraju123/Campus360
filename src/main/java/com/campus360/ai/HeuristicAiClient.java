package com.campus360.ai;

/**
 * Offline fallback so Campus360 Intelligence features work without an OpenAI key.
 * It echoes a structured, deterministic response derived from the prompt rather
 * than calling any external service. Swapped out for {@link SpringAiClient} when
 * {@code campus360.ai.enabled=true}.
 */
public class HeuristicAiClient implements AiClient {

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        return """
                [Campus360 Intelligence - offline mode]
                AI is currently disabled (campus360.ai.enabled=false), so this is a
                deterministic placeholder rather than a live model response.

                Request context:
                %s

                Enable OpenAI by setting AI_ENABLED=true and OPENAI_API_KEY to receive
                tailored, model-generated guidance here.
                """.formatted(truncate(userPrompt));
    }

    @Override
    public boolean isLive() {
        return false;
    }

    private static String truncate(String s) {
        if (s == null) {
            return "";
        }
        return s.length() <= 600 ? s : s.substring(0, 600) + "...";
    }
}
