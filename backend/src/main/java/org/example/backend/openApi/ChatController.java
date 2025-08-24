package org.example.backend.openApi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final MyOpenAiService aiService;

    // 일반 REST API
    @PostMapping("/ask")
    public String ask(@RequestBody PromptRequest request) {
        return aiService.ask(request.getPrompt());
    }

    @Setter
    @Getter
    public static class PromptRequest {
        private String prompt;

    }

}