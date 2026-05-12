package com.xinwen.ai.conifg.agent;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.xinwen.ai.ai.tools.agent.AgentMessageToCustomerServiceTools;
import com.xinwen.ai.ai.tools.agent.AgentProductAiTools;
import com.xinwen.ai.ai.tools.agent.Config;
import com.xinwen.ai.ai.tools.agent.data.MessageToCustomerServiceRequest;
import com.xinwen.ai.ai.tools.agent.node.PreprocessorNode;
import com.xinwen.ai.ai.tools.agent.node.ValidatorNode;
import com.xinwen.ai.conifg.constant.ProductMatchChatSystemPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StateGraphConfig {

    @Value("classpath:prompt/product-master.st")
    private Resource templateResource;

    private final ChatModel chatModel;

    private final AgentProductAiTools agentProductAiTools;

    private final AgentMessageToCustomerServiceTools agentMessageToCustomerServiceTools;

    @Bean
    public MemorySaver memorySaver() {
        return new MemorySaver();
    }

    @Bean("reactAgent")
    public ReactAgent reactAgentBuilder(MemorySaver memorySaver){
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
                .description("""
                        当用户询问系统里有哪些商品、当前有哪些商品、全部商品、商品列表、我们拥有的商品时，必须调用此工具。
                        禁止直接凭知识库或记忆回答商品列表。可传入用户输入的商品型号作为""")
                .inputType(MessageToCustomerServiceRequest.class)
                .build();

        return ReactAgent.builder()
                .name("product-match-agent")
                .model(chatModel)
                .systemPrompt(systemPrompt)
                .instruction("{input}")
                .saver(memorySaver)
                .outputKey("product_result")
                .tools(List.of(agentMessageToCustomerServiceTool, agentProductAiTool))
                .hooks(HumanInTheLoopHook.builder()
                        .approvalOn("agentMessageToCustomerServiceTools", ToolConfig.builder()
                                .description("转人工客服需要确认")
                                .build())
                        .build())
                .build();
    }

    @Bean
    public CompiledGraph stateGraph(ReactAgent reactAgent, MemorySaver memorySaver) throws GraphStateException {
        // 7. 构建工作流
        StateGraph workflow = new StateGraph(Config.keyStrategyFactory);

        // 添加普通Node
        workflow.addNode("preprocess", node_async(new PreprocessorNode()));
        workflow.addNode("validate", node_async(new ValidatorNode()));

        // 添加Agent Node（嵌套的ReactAgent）
        workflow.addNode(reactAgent.name(), reactAgent.asNode(
                true,   // includeContents: 传递父图的消息历史
                false   // includeReasoning: 不返回推理过程
        ));

        // 定义流程：预处理 -> Agent处理 -> 验证
        workflow.addEdge(StateGraph.START, "preprocess");
        workflow.addEdge("preprocess", reactAgent.name());
        workflow.addEdge(reactAgent.name(), "validate");

        // 条件边：验证通过则结束，否则重新处理
        workflow.addConditionalEdges(
                "validate",
                edge_async(state -> {
                    Boolean isValid = state.value("is_valid", false);
                    return isValid ? "end" : reactAgent.name();
                }),
                Map.of(
                        "end", StateGraph.END,
                        reactAgent.name(), reactAgent.name()
                )
        );

        return workflow.compile(
                CompileConfig.builder()
                        .saverConfig(SaverConfig.builder().register(memorySaver).build())
                        .build()
        );
    }
}
