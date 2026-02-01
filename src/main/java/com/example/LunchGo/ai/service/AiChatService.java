package com.example.LunchGo.ai.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ChatLanguageModel chatLanguageModel;

    public String chat(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "";
        }
        return chatLanguageModel.generate(prompt);
    }
}
