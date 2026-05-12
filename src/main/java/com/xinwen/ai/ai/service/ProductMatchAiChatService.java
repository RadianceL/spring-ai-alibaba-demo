package com.xinwen.ai.ai.service;

import reactor.core.publisher.Flux;

public interface ProductMatchAiChatService {

    Flux<String> stream(String timeId, String message) ;

    Flux<String> streamStateGraph(String timeId, String message);
}
