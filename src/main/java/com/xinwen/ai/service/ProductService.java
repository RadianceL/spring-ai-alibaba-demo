package com.xinwen.ai.service;

import com.xinwen.ai.repository.data.VmsProductPO;

import java.util.List;

public interface ProductService {

    List<VmsProductPO> findAllProduct(String userInputProductName);

}
