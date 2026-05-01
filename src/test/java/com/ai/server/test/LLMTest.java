package com.ai.server.test;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LLMTest {

    /**
     * gpt-4o-mini语言模型接入测试
     */
    @Test
    public void testGPTDemo() {

        //LangChain4j提供的代理服务器， 该代理服务器会将演示密钥替换成真实密钥， 再将请求转发给OpenAI API
        // 初始化模型
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();
        
        // 向模型提问
        String answer = model.chat("我国有多少个县城？");
        // 输出结果
        System.out.println(answer);
    }
}