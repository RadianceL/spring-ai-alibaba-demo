package com.xinwen.ai.service.impl;

import com.alibaba.fastjson.JSON;
import com.xinwen.ai.repository.ProductRepository;
import com.xinwen.ai.repository.data.VmsProductPO;
import com.xinwen.ai.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<VmsProductPO> findAllProduct(String userInputProductName) {
        log.info("开始查询: " + userInputProductName);
        List<VmsProductPO> allProduct = productRepository.findAllProduct(userInputProductName);
        log.info("系统里的商品清单：{}", JSON.toJSONString(allProduct));
        return allProduct;
    }
}
