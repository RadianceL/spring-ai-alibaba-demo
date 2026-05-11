package com.xinwen.ai.ai.tools.agent;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;

import java.util.HashMap;

public class Config {

    public static KeyStrategyFactory keyStrategyFactory = () -> {
        HashMap<String, KeyStrategy> strategies = new HashMap<>();
        strategies.put("input", new ReplaceStrategy());
        strategies.put("cleaned_input", new ReplaceStrategy());
        strategies.put("product_result", new ReplaceStrategy());
        strategies.put("is_valid", new ReplaceStrategy());
        return strategies;
    };

}
