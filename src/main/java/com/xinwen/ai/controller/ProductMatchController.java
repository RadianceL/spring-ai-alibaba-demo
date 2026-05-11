package com.xinwen.ai.controller;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.xinwen.ai.conifg.constant.ChatConfig;
import com.xinwen.ai.ai.ProductMatchAiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductMatchController {

    private final ProductMatchAiChatService productMatchAiChatService;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestParam("timeId") String timeId,
                           @RequestParam("message") String message) throws GraphRunnerException, GraphStateException {
        SseEmitter emitter = new SseEmitter(ChatConfig.CHAT_SSE_TIMEOUT_MS);
        MediaType textUtf8 = new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);

        AtomicReference<Disposable> subscriptionRef = new AtomicReference<>();
        Disposable subscription = productMatchAiChatService.stream(timeId, message).subscribe(chunk -> {
            try {
                emitter.send(SseEmitter.event().data(chunk, textUtf8));
            } catch (IOException e) {
                Disposable d = subscriptionRef.get();
                if (d != null) {
                    d.dispose();
                }
                emitter.completeWithError(e);
            }
        }, emitter::completeWithError, emitter::complete);
        subscriptionRef.set(subscription);

        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(() -> {
            subscription.dispose();
            emitter.complete();
        });
        return emitter;
    }
}
