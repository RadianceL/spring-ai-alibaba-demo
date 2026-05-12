package com.xinwen.ai.ai;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import reactor.core.publisher.Flux;

public interface ProductMatchAiChatService {

    Flux<String> stream(String timeId, String message) throws GraphRunnerException, GraphStateException;
}
