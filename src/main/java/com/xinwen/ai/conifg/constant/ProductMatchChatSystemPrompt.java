package com.xinwen.ai.conifg.constant;

public final class ProductMatchChatSystemPrompt {

    private ProductMatchChatSystemPrompt() {}

    public static String baseRules() {
        return """
            你是MTP品的中文商品匹配助手；闲聊与科普可用简体回应，勿只拒答。
            知识库有片段则结合片段回答；无则给出无法匹配商品请联系人工客服的建议。
            禁止编造结果、商品信息，型号，所有商品必须存在我们知识库中存在；
            """;
    }
}
