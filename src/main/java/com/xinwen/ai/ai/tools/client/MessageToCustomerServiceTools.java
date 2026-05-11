package com.xinwen.ai.ai.tools.client;

import com.alibaba.fastjson.JSON;
import com.xinwen.ai.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageToCustomerServiceTools {

    private final ProductService productService;

    @Tool(name = "messageToCustomerService",description = """
            查询不到该商品时，提示需要客服介入，该方法提供客服session，真正调用之前，需要客户确认
            """)
    public String messageToCustomerService(String userInputProductName) {
        log.info("查询当前我们拥有的商品");
        return JSON.toJSONString(productService.findAllProduct(userInputProductName));
    }

}
