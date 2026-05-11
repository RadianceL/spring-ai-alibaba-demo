package com.xinwen.ai.ai.tools.agent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

public class PreprocessorNode implements NodeAction {
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String input = state.value("input", "");
        String cleaned = input.trim();
        return Map.of("cleaned_input", cleaned);
    }
}
