package com.xinwen.ai.ai.service;

import com.alibaba.cloud.ai.graph.NodeOutput;
import reactor.core.publisher.Flux;

public interface ProductMatchAiChatService {

    Flux<String> stream(String timeId, String message);

    Flux<NodeOutput> streamStateGraph(String timeId, String message);
}
