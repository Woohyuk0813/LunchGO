package com.example.LunchGo.chatbot.controller;

import com.example.LunchGo.chatbot.dto.ChatbotMessageRequest;
import com.example.LunchGo.chatbot.service.NcpChatbotService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final NcpChatbotService chatbotService;

    @PostMapping("/open")
    public ResponseEntity<String> open(
            @RequestBody(required = false) ChatbotMessageRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String message = request == null ? null : request.getMessage();
        String userId = request == null ? null : request.getUserId();
        String userIp = httpServletRequest.getRemoteAddr();
        String response = chatbotService.open(message, userId, userIp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/message")
    public ResponseEntity<String> message(
            @RequestBody ChatbotMessageRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String userIp = httpServletRequest == null ? null : httpServletRequest.getRemoteAddr();
        String response = chatbotService.sendMessage(request.getMessage(), request.getUserId(), userIp);
        return ResponseEntity.ok(response);
    }
}
