package org.codewith3h.finmateapplication.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/chat")
@AllArgsConstructor
public class ChatBotController {

    ChatService chatService;

    @GetMapping
    public ResponseEntity<ApiResponse<String>> sendMessage(@RequestParam String chat) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(chatService.chat(chat));
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/stream")
    public Flux<String> sendMessageStream(@RequestParam String chat) {
         return chatService.chatStream(chat);
    }

}
