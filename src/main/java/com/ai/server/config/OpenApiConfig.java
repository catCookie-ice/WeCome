package com.ai.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ai-server接口文档") // 文档标题
                        .description("ai-server接口文档配置信息") // 文档描述
                        .version("v1.0.0") // 接口版本
                        .contact(new Contact() // 联系人信息
                                .name("bruce")
                                .email("3453453@qq.com")));
    }
}