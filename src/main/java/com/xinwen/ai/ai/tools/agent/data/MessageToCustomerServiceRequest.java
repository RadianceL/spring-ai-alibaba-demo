package com.xinwen.ai.ai.tools.agent.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageToCustomerServiceRequest {

    @JsonProperty(required = true)
    @JsonPropertyDescription("传入用户输入的商品型号或者品名或者商品描述")
    public String productCode;

}
