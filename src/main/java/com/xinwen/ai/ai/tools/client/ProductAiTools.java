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
public class ProductAiTools {

    private final ProductService productService;

    @Tool(description = """
            当用户询问系统里有哪些商品、当前有哪些商品、全部商品、商品列表、我们拥有的商品时，必须调用此工具。
                           禁止直接凭知识库或记忆回答商品列表。可传入用户输入的商品型号作为""")
    public String findAllProduct(String userInputProductName) {
        log.info("查询当前我们拥有的商品");
        return JSON.toJSONString(productService.findAllProduct(userInputProductName));
    }

}
