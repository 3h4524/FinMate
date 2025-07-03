package org.codewith3h.finmateapplication.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {
    ChatClient chatClient;
//    TransactionService transactionService;
//    GoalService goalService;

    public String chat(String chat) {
        return chatClient
                .prompt(chat)
                .call()
                .content();
    }


    public Flux<String> chatStream(String chat) {
        return chatClient
                .prompt(chat)
                .stream()
                .content();
    }
}
