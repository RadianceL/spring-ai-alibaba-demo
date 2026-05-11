package com.xinwen.ai.ai.impl;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.xinwen.ai.ai.tools.MessageToCustomerServiceTools;
import com.xinwen.ai.ai.tools.ProductAiTools;
import com.xinwen.ai.conifg.constant.ProductMatchChatSystemPrompt;
import com.xinwen.ai.ai.ProductMatchAiChatService;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ProductMatchAiChatServiceImpl implements ProductMatchAiChatService {

    @Value("classpath:prompt/product-master.st")
    private Resource templateResource;

    private final ChatModel chatModel;

    private final MessageToCustomerServiceTools messageToCustomerServiceTools;

    private final ProductAiTools productAiTools;

    public ProductMatchAiChatServiceImpl(ChatModel chatModel, MessageToCustomerServiceTools messageToCustomerServiceTools, ProductAiTools productAiTools) {
        this.chatModel = chatModel;
        this.messageToCustomerServiceTools = messageToCustomerServiceTools;
        this.productAiTools = productAiTools;
    }

    @Override
    public Flux<String> stream(String timeId, String message) throws GraphRunnerException {
        PromptTemplate template = new PromptTemplate(templateResource);
        String systemPrompt = template.render(Map.of(
                "baseRules", ProductMatchChatSystemPrompt.baseRules(),
                "serverDate", LocalDate.now().toString())
        );

        ReactAgent agent = ReactAgent.builder()
                .name("product-match-agent")
                .model(chatModel)
                .instruction(systemPrompt)
                .saver(new MemorySaver())
                .tools(List.of(productAiTools, messageToCustomerServiceTools))
                .hooks(HumanInTheLoopHook.builder()
                        .approvalOn("messageToCustomerService", ToolConfig.builder()
                                .description("转人工客服需要确认")
                                .build())
                        .build())
                .build();

        RunnableConfig config = RunnableConfig.builder()
                .threadId(timeId)
                .build();

        return agent.streamMessages(message, config)
                .filter(msg -> msg instanceof AssistantMessage)
                .map(msg -> ((AssistantMessage) msg).getText())
                .filter(s -> s != null && !s.isEmpty());
    }
}
