package com.xinwen.ai.ai.service.impl;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.xinwen.ai.ai.agent.StateGraphConfig;
import com.xinwen.ai.ai.service.ProductMatchAiChatService;
import com.xinwen.ai.ai.tools.spring.tools.MessageToCustomerServiceTools;
import com.xinwen.ai.ai.tools.spring.tools.ProductAiTools;
import com.xinwen.ai.ai.config.ProductMatchChatSystemPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReactAgentProductMatchAiChatServiceImpl implements ProductMatchAiChatService {

    private final CompiledGraph compiledGraph;

    @Value("classpath:prompt/product-master.st")
    private Resource templateResource;

    private final ChatClient chatClient;

    private final MessageToCustomerServiceTools messageToCustomerServiceTools;

    private final ProductAiTools productAiTools;

    @Override
    public Flux<String> stream(String timeId, String message) {
        PromptTemplate template = new PromptTemplate(templateResource);
        String systemPrompt = template.render(Map.of(
                "baseRules", ProductMatchChatSystemPrompt.baseRules(),
                "serverDate", LocalDate.now().toString())
        );

        return chatClient.prompt()
                .system(systemPrompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, timeId))
                .tools(messageToCustomerServiceTools, productAiTools)
                .user(message)
                .stream()
                .content();
    }

    @Override
    public Flux<NodeOutput> streamStateGraph(String timeId, String message) {
//        Map<String, Object> input = Map.of(StateGraphConfig.USER_INPUT, message);
//
//        // 第一次调用 - 可能触发中断
//        Optional<NodeOutput> nodeOutputOptional = compiledGraph.invokeAndGetOutput(
//                input, RunnableConfig.builder().threadId(timeId).build()
//        );
//
////         检查是否发生中断
//        if (nodeOutputOptional.isPresent()
//                && nodeOutputOptional.get() instanceof InterruptionMetadata interruptionMetadata) {
//
//            System.out.println("工作流被中断，等待人工审核。");
//            System.out.println("中断节点: " + interruptionMetadata.node());
//
//            List<InterruptionMetadata.ToolFeedback> feedbacks = interruptionMetadata.toolFeedbacks();
//
//            // 显示所有需要审批的工具调用
//            for (InterruptionMetadata.ToolFeedback feedback : feedbacks) {
//                System.out.println("工具名称: " + feedback.getName());
//                System.out.println("工具参数: " + feedback.getArguments());
//                System.out.println("工具描述: " + feedback.getDescription());
//            }
//
//            // 构建人工反馈（批准所有工具调用）
//            InterruptionMetadata.Builder feedbackBuilder = InterruptionMetadata.builder()
//                    .nodeId(interruptionMetadata.node())
//                    .state(interruptionMetadata.state());
//
//            feedbacks.forEach(toolFeedback -> {
//                feedbackBuilder.addToolFeedback(
//                        InterruptionMetadata.ToolFeedback.builder(toolFeedback)
//                                .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
//                                .build()
//                );
//            });
//
//            InterruptionMetadata approvalMetadata = feedbackBuilder.build();
//
//            // 使用批准决策恢复执行
//            RunnableConfig resumableConfig = RunnableConfig.builder()
//                    .threadId(timeId) // 相同的线程ID
//                    .addHumanFeedback(approvalMetadata)
//                    .build();
//
//            // 恢复工作流执行（传入空Map，因为状态已保存在检查点中）
//            return compiledGraph.stream(Map.of(), resumableConfig);
//        }
        RunnableConfig resumableConfig = RunnableConfig.builder()
                .threadId(timeId) // 相同的线程ID
                .build();

        // 流式执行，实时获取每个节点的输出
        Flux<NodeOutput> lastOutput = compiledGraph.stream(Map.of(StateGraphConfig.USER_INPUT, message), resumableConfig);
//        if (Objects.isNull(lastOutput)) {
//            return Flux.empty();
//        }
//        if (lastOutput.state().data().get(StateGraphConfig.ASSISTANT_RESULT) instanceof AssistantMessage assistantMessage) {
//            if (StringUtils.isNotBlank(assistantMessage.getText())) {
//                return Flux.just(assistantMessage.getText());
//            }
//        }
        return lastOutput;
    }
}
