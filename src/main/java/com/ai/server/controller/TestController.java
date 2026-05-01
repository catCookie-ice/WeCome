package com.ai.server.controller;

import com.ai.server.common.Result;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接入chat GPT-AI平台
 */
@RestController
@Slf4j
@Tag(name = "测试GPT控制器", description = "测试GPT控制器") // 类级注解
public class TestController {

    @Operation(summary = "测试SpringBoot项目", description = "测试SpringBoot项目") // 方法级注解
    @GetMapping("/test")
    public String test(){
        return "Hello,Spring Boot!";
    }

    /**
     * 整合SpringBoot
     */
    @Autowired
    private OpenAiChatModel openAiChatModel;

    @Operation(summary = "测试GPT-AI平台接口", description = "测试GPT-AI平台接口") // 方法级注解
    @GetMapping("/test1/{msg}")
    public Result testSpringBoot(@PathVariable("msg") String msg) {
        // 向模型提问
        String answer = openAiChatModel.chat(msg);
        // 输出结果
        log.info("answer: {}", answer);
        return Result.success(answer);
    }

}
