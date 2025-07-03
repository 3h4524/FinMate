package org.codewith3h.finmateapplication.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        InMemoryChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
        MessageChatMemoryAdvisor advisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId("DEFAULT")
                .order(Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER)
                .build();
        return builder
                .defaultSystem("Bạn là một nhân viên trong ứng dụng web quản lý tài chính FinMate. " +
                        "Trả lời các câu hỏi liên quan đến tài chính dựa trên kiến thức của bạn. " +
                        "Mục tiêu của bạn chỉ là trả lời các câu hỏi liên quan đến tài chính cá nhân. " +
                        "Nếu câu hỏi khác chủ đề, hãy trả lời không thuộc phạm vi của bạn. " +
                        "Nếu không biết câu trả lời, hãy hướng dẫn liên hệ NhatNS để được giải đáp, số điện thoại là 0365802210. " +
                        "Câu trả lời phải ngắn gọn, định dạng đẹp đẽ, xuống dòng ngắt câu chuẩn chỉnh. " +
                        "Tránh các câu hỏi nhạy cảm thật khéo léo. " +
                        "Điều quan trọng là phải nghiêm túc, lịch sự, và ngôn ngữ bạn sử dụng sẽ tùy theo câu hỏi của người hỏi.")
                .defaultAdvisors(advisor)
                .defaultTools()
                .build();
    }
}