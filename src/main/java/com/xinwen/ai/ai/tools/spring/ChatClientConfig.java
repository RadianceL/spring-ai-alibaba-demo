package com.xinwen.ai.ai.tools.spring;

import com.xinwen.ai.ai.tools.spring.tools.MessageToCustomerServiceTools;
import com.xinwen.ai.ai.tools.spring.tools.ProductAiTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChatClientConfig {

    @Value("classpath:prompt/product-master.st")
    private Resource templateResource;

    private final MessageToCustomerServiceTools messageToCustomerServiceTools;

    private final ProductAiTools productAiTools;

    private final Advisor retrievalAugmentationAdvisor;

    private final MessageChatMemoryAdvisor messageChatMemoryAdvisor;


    @Bean
    public ChatClient buildChatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
                .defaultAdvisors(retrievalAugmentationAdvisor, messageChatMemoryAdvisor)
                .build();
    }
}
