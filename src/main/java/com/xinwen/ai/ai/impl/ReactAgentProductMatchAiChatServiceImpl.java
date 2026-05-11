package com.xinwen.ai.ai.impl;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.xinwen.ai.ai.ProductMatchAiChatService;
import com.xinwen.ai.ai.tools.agent.AgentMessageToCustomerServiceTools;
import com.xinwen.ai.ai.tools.agent.AgentProductAiTools;
import com.xinwen.ai.ai.tools.agent.Config;
import com.xinwen.ai.ai.tools.agent.data.MessageToCustomerServiceRequest;
import com.xinwen.ai.ai.tools.agent.node.PreprocessorNode;
import com.xinwen.ai.ai.tools.agent.node.ValidatorNode;
import com.xinwen.ai.conifg.constant.ProductMatchChatSystemPrompt;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

@Service("reactAgentProductMatchAiChatServiceImpl")
public class ReactAgentProductMatchAiChatServiceImpl implements ProductMatchAiChatService {

    @Value("classpath:prompt/product-master.st")
    private Resource templateResource;

    private final ChatModel chatModel;

    private final AgentProductAiTools agentProductAiTools;

    private final AgentMessageToCustomerServiceTools agentMessageToCustomerServiceTools;

    public ReactAgentProductMatchAiChatServiceImpl(ChatModel chatModel, AgentProductAiTools agentProductAiTools, AgentMessageToCustomerServiceTools agentMessageToCustomerServiceTools) {
        this.chatModel = chatModel;
        this.agentProductAiTools = agentProductAiTools;
        this.agentMessageToCustomerServiceTools = agentMessageToCustomerServiceTools;
    }

    @Override
    public Flux<String> stream(String timeId, String message) throws GraphRunnerException, GraphStateException {
        PromptTemplate template = new PromptTemplate(templateResource);
        String systemPrompt = template.render(Map.of(
                "baseRules", ProductMatchChatSystemPrompt.baseRules(),
                "serverDate", LocalDate.now().toString())
        );

        ToolCallback agentMessageToCustomerServiceTool = FunctionToolCallback.builder("agentMessageToCustomerServiceTools", agentMessageToCustomerServiceTools)
                .description("当用户需要联系客服人员，必须调用此工具")
                .inputType(MessageToCustomerServiceRequest.class)
                .build();

        ToolCallback agentProductAiTool = FunctionToolCallback.builder("agentProductAiTool", agentProductAiTools)
                .description("当用户询问系统里有哪些商品、当前有哪些商品、全部商品、商品列表、我们拥有的商品时，必须调用此工具。" +
                        "禁止直接凭知识库或记忆回答商品列表。传入用户要查询的商品名称或者型号或者其他表示商品代号的描述")
                .inputType(MessageToCustomerServiceRequest.class)
                .build();

        MemorySaver memorySaver = new MemorySaver();
        ReactAgent agent = ReactAgent.builder()
                .name("product-match-agent")
                .model(chatModel)
                .instruction(systemPrompt)
                .saver(memorySaver)
                .outputKey("product_result")
                .tools(List.of(agentMessageToCustomerServiceTool, agentProductAiTool))
                .hooks(HumanInTheLoopHook.builder()
                        .approvalOn("agentProductAiTool", ToolConfig.builder()
                                .description("转人工客服需要确认")
                                .build())
                        .build())
                .build();

        RunnableConfig config = RunnableConfig.builder()
                .threadId(timeId)
                .build();


        // 7. 构建工作流
        StateGraph workflow = new StateGraph(Config.keyStrategyFactory);

        // 添加普通Node
        workflow.addNode("preprocess", node_async(new PreprocessorNode()));
        workflow.addNode("validate", node_async(new ValidatorNode()));

        // 添加Agent Node（嵌套的ReactAgent）
        workflow.addNode(agent.name(), agent.asNode(
                true,   // includeContents: 传递父图的消息历史
                false   // includeReasoning: 不返回推理过程
        ));


        // 定义流程：预处理 -> Agent处理 -> 验证
        workflow.addEdge(StateGraph.START, "preprocess");
        workflow.addEdge("preprocess", agent.name());
        workflow.addEdge(agent.name(), "validate");

        // 条件边：验证通过则结束，否则重新处理
        workflow.addConditionalEdges(
                "validate",
                edge_async(state -> {
                    Boolean isValid = state.value("is_valid", false);
                    return isValid ? "end" : agent.name();
                }),
                Map.of(
                        "end", StateGraph.END,
                        agent.name(), agent.name()
                )
        );

        CompiledGraph compiledGraph = workflow.compile(
                CompileConfig.builder()
                        .saverConfig(SaverConfig.builder().register(memorySaver).build())
                        .build()
        );

        Map<String, Object> input = Map.of("input", message);

        // 第一次调用 - 可能触发中断
        Optional<NodeOutput> nodeOutputOptional = compiledGraph.invokeAndGetOutput(
                input,
                RunnableConfig.builder().threadId(timeId).build()
        );


        // 检查是否发生中断
        if (nodeOutputOptional.isPresent()
                && nodeOutputOptional.get() instanceof InterruptionMetadata interruptionMetadata) {

            System.out.println("工作流被中断，等待人工审核。");
            System.out.println("中断节点: " + interruptionMetadata.node());

            List<InterruptionMetadata.ToolFeedback> feedbacks = interruptionMetadata.toolFeedbacks();

            // 显示所有需要审批的工具调用
            for (InterruptionMetadata.ToolFeedback feedback : feedbacks) {
                System.out.println("工具名称: " + feedback.getName());
                System.out.println("工具参数: " + feedback.getArguments());
                System.out.println("工具描述: " + feedback.getDescription());
            }

            // 构建人工反馈（批准所有工具调用）
            InterruptionMetadata.Builder feedbackBuilder = InterruptionMetadata.builder()
                    .nodeId(interruptionMetadata.node())
                    .state(interruptionMetadata.state());

            feedbacks.forEach(toolFeedback -> {
                feedbackBuilder.addToolFeedback(
                        InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                                .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                                .build()
                );
            });

            InterruptionMetadata approvalMetadata = feedbackBuilder.build();

            // 使用批准决策恢复执行
            RunnableConfig resumableConfig = RunnableConfig.builder()
                    .threadId(timeId) // 相同的线程ID
                    .addHumanFeedback(approvalMetadata)
                    .build();

            // 恢复工作流执行（传入空Map，因为状态已保存在检查点中）
            return compiledGraph.stream(Map.of(), resumableConfig)
                    .map(nodeOutput -> {
                        if (nodeOutput.isEND() && nodeOutput.state().data().get("product_result") instanceof AssistantMessage assistantMessage) {

                            return assistantMessage.getText();
                        }
                        return "";
                    })
                    .filter(s -> !s.isBlank());
        }
        return agent.streamMessages(message, config)
                .map(msg -> {
                    // 正常AI消息返回
                    if (msg instanceof AssistantMessage) {
                        return ((AssistantMessage) msg).getText();
                    }
                    return "";
                })
                .filter(s -> s != null && !s.isBlank());
    }
}
