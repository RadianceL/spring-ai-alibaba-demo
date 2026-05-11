package com.xinwen.ai.repository;

import com.xinwen.ai.repository.data.VmsProductPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository {

    List<VmsProductPO> findAllProduct(String userInputProductName);
}
