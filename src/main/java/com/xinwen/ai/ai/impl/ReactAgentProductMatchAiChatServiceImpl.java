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
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.xinwen.ai.ai.ProductMatchAiChatService;
import com.xinwen.ai.ai.tools.agent.AgentMessageToCustomerServiceTools;
import com.xinwen.ai.ai.tools.agent.AgentProductAiTools;
import com.xinwen.ai.ai.tools.agent.Config;
import com.xinwen.ai.ai.tools.agent.data.MessageToCustomerServiceRequest;
import com.xinwen.ai.ai.tools.agent.node.PreprocessorNode;
import com.xinwen.ai.ai.tools.agent.node.ValidatorNode;
import com.xinwen.ai.conifg.constant.ProductMatchChatSystemPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReactAgentProductMatchAiChatServiceImpl implements ProductMatchAiChatService {

    private final CompiledGraph compiledGraph;

    @Override
    public Flux<String> stream(String timeId, String message) {
        Map<String, Object> input = Map.of("input", message);

        // 第一次调用 - 可能触发中断
        Optional<NodeOutput> nodeOutputOptional = compiledGraph.invokeAndGetOutput(
                input, RunnableConfig.builder().threadId(timeId).build()
        );

//         检查是否发生中断
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
        RunnableConfig resumableConfig = RunnableConfig.builder()
                .threadId(timeId) // 相同的线程ID
                .build();
//        return compiledGraph.stream(Map.of(), resumableConfig)
//                .map(nodeOutput -> {
//                    if (nodeOutput.isEND() && nodeOutput.state().data().get("product_result") instanceof AssistantMessage assistantMessage) {
//
//                        return assistantMessage.getText();
//                    }
//                    return "";
//                })
//                .filter(s -> !s.isBlank());

// 流式执行，实时获取每个节点的输出
        NodeOutput lastOutput = compiledGraph.stream(Map.of("input", message), resumableConfig)
                .doOnNext(output -> {
                    if (output instanceof StreamingOutput<?> streamingOutput) {
                        if (streamingOutput.message() != null) {
                            // streaming output from streaming llm node
//                            System.out.println("Streaming output from node " + streamingOutput.node() + ": " + streamingOutput.message().getText());
                        } else {
                            // output from normal node, investigate the state to get the node data
//                            System.out.println("Output from node " + streamingOutput.node() + ": " + streamingOutput.state().data());
                        }
                    }
                })
                .blockLast();
        return lastOutput.state().data().get("product_result") instanceof AssistantMessage assistantMessage ?
                Flux.just(assistantMessage.getText()) : Flux.empty();
    }
}
