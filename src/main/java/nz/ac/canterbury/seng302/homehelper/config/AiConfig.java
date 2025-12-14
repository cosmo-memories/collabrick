package nz.ac.canterbury.seng302.homehelper.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.MysqlChatMemoryRepositoryDialect;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Configuration class for setting up AI-related beans.
 */
@Configuration
public class AiConfig {

    /**
     * Provides a ChatMemoryRepository backed by MySQL for storing chat history.
     * This bean is only active under the production and staging profiles.
     *
     * @param jdbcTemplate the JdbcTemplate used for database access.
     * @return a JdbcChatMemoryRepository configured for MySQL.
     */
    @Bean
    @Profile({"production", "staging"})
    public ChatMemoryRepository mySqlChatMemoryRepository(JdbcTemplate jdbcTemplate) {
        return JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dialect(new MysqlChatMemoryRepositoryDialect())
                .build();
    }

    /**
     * Provides an in-memory ChatMemoryRepository if no other repository bean exists.
     * To be used for testing or development environments.
     *
     * @return an InMemoryChatMemoryRepository.
     */
    @Bean
    @ConditionalOnMissingBean(ChatMemoryRepository.class)
    public ChatMemoryRepository inMemoryChatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    /**
     * Creates a ChatMemory component that stores a limited number of messages in the configured
     * ChatMemoryRepository bean.
     *
     * @param chatMemoryRepository the repository used to store the chat history.
     * @return a MessageWindowChatMemory with a maximum of 10 messages.
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
    }

    /**
     * Creates a ChatClient that integrates the AI model with memory management.
     *
     * @param chatModel  the OpenAiChatModel used to generate AI responses.
     * @param chatMemory the ChatMemory that maintains conversation context.
     * @return a configured ChatClient.
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
