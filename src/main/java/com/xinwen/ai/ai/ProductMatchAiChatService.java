package com.xinwen.ai.ai;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

public interface ProductMatchAiChatService {

    Flux<String> stream(String timeId, String message) throws GraphRunnerException;
}
