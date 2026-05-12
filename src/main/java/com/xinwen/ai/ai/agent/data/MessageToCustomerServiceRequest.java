package com.xinwen.ai.ai.agent.data;

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
    @JsonPropertyDescription("传入用户想要查询的商品名称或者商品编码，用户输入什么就查询什么，不要擅自改动")
    public String productCode;

}
