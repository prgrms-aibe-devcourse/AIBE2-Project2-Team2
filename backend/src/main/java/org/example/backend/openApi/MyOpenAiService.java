package org.example.backend.openApi;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.List;

@Service
public class MyOpenAiService {

    private final OpenAiService openAiService;

    public MyOpenAiService(@Value("${openai.api-key}") String apiKey) {
        // yml 에서 불러온 키 사용
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(30));
    }

    // REST API 방식 (단발성 호출)
    public String ask(String prompt) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(List.of(new ChatMessage("user", prompt)))
                .maxTokens(500)
                .build();

        ChatCompletionResult result = openAiService.createChatCompletion(request);

        return result.getChoices().get(0).getMessage().getContent();
    }
}
