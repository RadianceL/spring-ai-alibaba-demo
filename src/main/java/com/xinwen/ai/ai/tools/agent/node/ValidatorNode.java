package com.xinwen.ai.ai.tools.agent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

public class ValidatorNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
//        Optional<Object> qaResultOpt = state.value("product_result");
//        if (qaResultOpt.isPresent() && qaResultOpt.get() instanceof Message message) {
//            boolean isValid = message.getText().length() > 50000;
//            return Map.of("is_valid", isValid);
//        }
        return Map.of("is_valid", true);
    }
}
