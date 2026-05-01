package com.ai.server.controller;


import com.ai.server.common.Result;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@Slf4j
@Tag(name = "测试API", description = "测试相关接口")
//默认路径为/CC
@RequestMapping("/CC")
public class Controller {
    @GetMapping
    @Operation(summary = "启动测试", description = "用于测试是否启动")
    public String index() {
        return "Hello World!";
    }

    /**
     * 整合SpringBoot
     */
    @Autowired
    private OpenAiChatModel openAiChatModel;

    @Operation(summary = "测试接口1-1", description = "通过路径变量传递消息")
    //由于前后端分离，允许跨域访问
    @GetMapping("/Ctest1/{message}")//是 Spring MVC 中的一个注解，用于映射 HTTP GET 请求到对应的处理方法，
//表示该方法映射的请求路径为 /test1/xxx（其中 xxx 是动态变化的部分）：
//{msg} 是路径变量（Path Variable），用于捕获 URL 中该位置的动态值（例如请求 /test1/hello 时，msg 的值为 hello）。
//通常配合方法参数上的 @PathVariable 注解获取路径变量的值,如下:
    public Result testSpringBoot_1(@Parameter(description = "消息内容") @PathVariable("message") String msg) {//处理url格式为 /test1/xxx的请求

        // 向模型提问
        String answer = openAiChatModel.chat(msg);
        // 输出结果
        log.info("answer: {}", answer);

        return Result.success(answer);
    }

//    @Operation(summary = "测试接口1-2", description = "通过key？=传递消息")
//    @GetMapping("/Ctest1")//处理url格式为 /test1?text=xxx的请求
//    public String testSpringBoot_2(@RequestParam("text") String msg) { // 用@RequestParam接收?text=xxx
//        log.info("收到参数: {}", msg); // 打印日志确认参数是否接收
//        String answer = openAiChatModel.chat(msg);
//        // 输出结果
//        log.info("answer: {}", answer);
//
//        return answer;
//    }
}


