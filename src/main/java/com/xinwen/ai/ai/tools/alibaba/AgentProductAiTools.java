package com.xinwen.ai.ai.tools.alibaba;

import com.alibaba.fastjson.JSON;
import com.xinwen.ai.ai.agent.data.MessageToCustomerServiceRequest;
import com.xinwen.ai.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AgentProductAiTools  implements BiFunction<MessageToCustomerServiceRequest, ToolContext, String> {

    private final ProductService productService;

    @Override
    public String apply(MessageToCustomerServiceRequest message, ToolContext toolContext) {
        log.info("查询当前我们拥有的商品");
        return JSON.toJSONString(productService.findAllProduct(message.getProductCode()));
    }
}
