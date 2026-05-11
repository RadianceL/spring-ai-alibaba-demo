package com.xinwen.ai.conifg.agent;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductMatchAgent {
    /**
     * 智能体模型
     */
    private final ChatModel chatModel;

    @Bean
    public ReactAgent  buildProductMatchReactAgent() {
        return ReactAgent.builder()
                .name("productMatchAgent")
                .model(chatModel)
                .build();
    }
}
