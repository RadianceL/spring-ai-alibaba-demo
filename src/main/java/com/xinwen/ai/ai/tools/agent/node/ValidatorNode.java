package com.xinwen.ai.ai.tools.agent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.messages.Message;

import java.util.Map;
import java.util.Optional;

public class ValidatorNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        Optional<Object> qaResultOpt = state.value("product_result");
        if (qaResultOpt.isPresent() && qaResultOpt.get() instanceof Message message) {
            boolean isValid = message.getText().length() > 30;
            return Map.of("is_valid", isValid);
        }
        return Map.of("is_valid", false);
    }
}
