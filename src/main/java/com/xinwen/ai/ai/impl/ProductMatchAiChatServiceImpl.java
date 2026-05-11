//package com.xinwen.ai.ai.impl;
//
//import com.xinwen.ai.ai.tools.client.MessageToCustomerServiceTools;
//import com.xinwen.ai.ai.tools.client.ProductAiTools;
//import com.xinwen.ai.conifg.constant.ProductMatchChatSystemPrompt;
//import com.xinwen.ai.ai.ProductMatchAiChatService;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
//import org.springframework.ai.chat.client.advisor.api.Advisor;
//import org.springframework.ai.chat.memory.ChatMemory;
//import org.springframework.ai.chat.prompt.PromptTemplate;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//
//import java.time.LocalDate;
//import java.util.Map;
//
//@Service("productMatchAiChatServiceImpl")
//public class ProductMatchAiChatServiceImpl implements ProductMatchAiChatService {
//
//    @Value("classpath:prompt/product-master.st")
//    private Resource templateResource;
//
//    private final ChatClient chatClient;
//
//    private final MessageToCustomerServiceTools messageToCustomerServiceTools;
//
//    private final ProductAiTools productAiTools;
//
//    public ProductMatchAiChatServiceImpl(ChatClient.Builder chatClient, Advisor retrievalAugmentationAdvisor,
//                                                   MessageChatMemoryAdvisor messageChatMemoryAdvisor, MessageToCustomerServiceTools messageToCustomerServiceTools, ProductAiTools productAiTools) {
//        this.messageToCustomerServiceTools = messageToCustomerServiceTools;
//        this.productAiTools = productAiTools;
//        this.chatClient = chatClient
//                .defaultAdvisors(retrievalAugmentationAdvisor, messageChatMemoryAdvisor)
//                .build();
//    }
//
//    @Override
//    public Flux<String> stream(String timeId, String message) {
//        PromptTemplate template = new PromptTemplate(templateResource);
//        String systemPrompt = template.render(Map.of(
//                "baseRules", ProductMatchChatSystemPrompt.baseRules(),
//                "serverDate", LocalDate.now().toString())
//        );
//
//        return chatClient.prompt()
//                .system(systemPrompt)
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, timeId))
//                .tools(messageToCustomerServiceTools, productAiTools)
//                .user(message)
//                .stream()
//                .content();
//    }
//}
