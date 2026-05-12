package com.xinwen.ai.ai.tools.alibaba;

import com.xinwen.ai.ai.agent.data.MessageToCustomerServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AgentMessageToCustomerServiceTools implements BiFunction<MessageToCustomerServiceRequest, ToolContext, String> {

    @Override
    public String apply(MessageToCustomerServiceRequest message, ToolContext toolContext) {
        log.info("客户认可，返回客户session");
        return "客服介入";
    }
}
